package net.xolt.freecam.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.UnixDomainSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DiscordRpc {
   private static final Logger LOGGER = LoggerFactory.getLogger("Freecam Discord RPC");
   private static final DiscordRpc INSTANCE = new DiscordRpc();
   private static final String APPLICATION_ID_PROPERTY = "freecam.discord.applicationId";
   private static final String APPLICATION_ID_ENVIRONMENT = "FREECAM_DISCORD_APPLICATION_ID";
   private static final String DEFAULT_APPLICATION_ID = "1553133011833323691";
   private static final long UPDATE_INTERVAL_MILLIS = 1000L;
   private static final long SERVER_ROTATION_INTERVAL_MILLIS = 5000L;
   private static final long ONLINE_UPDATE_INTERVAL_MILLIS = 5000L;
   private static final long SESSION_START = System.currentTimeMillis() / 1000L;
   private static final URI ONLINE_ENDPOINT = URI.create("https://moonvale.ru/api/online");
   private static final Pattern PLAYERS_PATTERN = Pattern.compile("\"players\"\\s*:\\s*(\\d+)");
   private static final String PARKOUR_STARTED_MESSAGE = "Паркур начат! Попробуйте побить свой рекорд";
   private static final String PARKOUR_ENDED_MESSAGE = "Вы упали! Ваш результат";
   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3L)).build();
   private final AtomicBoolean running = new AtomicBoolean();
   private volatile boolean inWorld;
   private volatile boolean onMoonValeServer;
   private volatile boolean inLobby;
   private volatile boolean inParkour;
   private volatile int onlinePlayers = -1;
   private volatile long nextOnlineUpdateMillis;
   private volatile String playerHeadImage = "minecraft";
   private volatile String playerName = "Minecraft";
   private volatile DiscordRpc.IpcConnection connection;
   private Thread worker;
   private boolean configuredEnabled;

   private DiscordRpc() {
      ClientReceiveMessageEvents.GAME.register(this::onGameMessage);
   }

   public static void start() {
      setEnabled(true);
   }

   public static synchronized void setEnabled(boolean enabled) {
      if (INSTANCE.configuredEnabled != enabled) {
         INSTANCE.configuredEnabled = enabled;
         if (enabled) {
            INSTANCE.startInternal();
         } else {
            INSTANCE.stopInternal();
         }
      }
   }

   public static void update(Minecraft client) {
      INSTANCE.inWorld = isInWorld(client);
      INSTANCE.onMoonValeServer = INSTANCE.inWorld && isMoonValeServer(client);
      if (!INSTANCE.onMoonValeServer) {
         INSTANCE.inParkour = false;
      }

      INSTANCE.inLobby = INSTANCE.onMoonValeServer && hasSurvivalLobbyStar(client);
      INSTANCE.updatePlayerIdentity(client);
   }

   public static void stop() {
      setEnabled(false);
   }

   private synchronized void startInternal() {
      if (!this.running.get()) {
         String applicationId = applicationId();
         if (applicationId.isBlank()) {
            LOGGER.warn("Discord RPC is disabled: set -D{} or {}.", "freecam.discord.applicationId", "FREECAM_DISCORD_APPLICATION_ID");
         } else {
            this.running.set(true);
            this.worker = new Thread(() -> this.run(applicationId), "freecam-discord-rpc");
            this.worker.setDaemon(true);
            this.worker.start();
         }
      }
   }

   private synchronized void stopInternal() {
      if (this.running.getAndSet(false)) {
         this.closeConnection();
         Thread current = this.worker;
         if (current != null) {
            current.interrupt();
         }

         this.worker = null;
      }
   }

   private void run(String applicationId) {
      while (this.running.get()) {
         try {
            DiscordRpc.IpcConnection ipc = connect();
            this.connection = ipc;
            send(ipc, 0, "{\"v\":1,\"client_id\":\"" + escape(applicationId) + "\"}");
            readFrame(ipc);

            for (long nextUpdate = 0L; this.running.get(); Thread.sleep(250L)) {
               long now = System.currentTimeMillis();
               if (now >= nextUpdate) {
                  this.sendActivity(ipc);
                  readFrame(ipc);
                  nextUpdate = now + 1000L;
               }
            }
         } catch (InterruptedException var11) {
            Thread.currentThread().interrupt();
            break;
         } catch (RuntimeException | IOException var12) {
            if (this.running.get()) {
               LOGGER.debug("Discord RPC is unavailable: {}", var12.getMessage());
               this.sleepBeforeReconnect();
            }
         } finally {
            this.closeConnection();
         }
      }
   }

   private void sendActivity(DiscordRpc.IpcConnection ipc) throws IOException {
      if (!this.onMoonValeServer) {
         clearActivity(ipc);
      } else {
         long now = System.currentTimeMillis();
         String details = this.inParkour ? "Играет в паркур" : (this.inLobby ? "В лобби" : "Играет на сервере");
         String serverAddress = now / 5000L % 2L == 0L ? "mc.moonvale.fun⠀" : "play.moonvale.ru⠀";
         int players = this.fetchOnlinePlayers();
         String playerCount = players >= 0 ? Integer.toString(players) : "?";
         String largeImage = "https://api.mcsrvstat.us/icon/play.moonvale.ru";
         String activity = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":"
            + ProcessHandle.current().pid()
            + ",\"activity\":{\"details\":\""
            + escape(details)
            + "\",\"state\":\""
            + playerCount
            + " • "
            + serverAddress
            + "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\",\"timestamps\":{\"start\":"
            + SESSION_START
            + "},\"party\":{},\"assets\":{\"large_image\":\""
            + escape(largeImage)
            + "\",\"large_text\":\"MoonVale\",\"small_image\":\""
            + escape(this.playerHeadImage)
            + "\",\"small_text\":\""
            + escape(this.playerName)
            + "\"},\"buttons\":[{\"label\":\"Сервер MoonVale\",\"url\":\"https://moonvale.ru/\"}]}},\"nonce\":\""
            + UUID.randomUUID()
            + "\"}";
         send(ipc, 1, activity);
      }
   }

   private void onGameMessage(Component message, boolean overlay) {
      if (message != null) {
         String text = message.getString();
         if (text.contains("Паркур начат! Попробуйте побить свой рекорд")) {
            this.inParkour = true;
         } else if (text.contains("Вы упали! Ваш результат")) {
            this.inParkour = false;
         }
      }
   }

   private static void clearActivity(DiscordRpc.IpcConnection ipc) throws IOException {
      String payload = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":"
         + ProcessHandle.current().pid()
         + ",\"activity\":null},\"nonce\":\""
         + UUID.randomUUID()
         + "\"}";
      send(ipc, 1, payload);
   }

   private void updatePlayerIdentity(Minecraft client) {
      try {
         Object profile = client.getClass().getMethod("getGameProfile").invoke(client);
         if (profile == null) {
            return;
         }

         Object id = invokeFirst(profile, "id", "getId");
         Object name = invokeFirst(profile, "name", "getName");
         if (id != null) {
            this.playerHeadImage = "https://mc-heads.net/avatar/" + id;
         }

         if (name != null && !name.toString().isBlank()) {
            this.playerName = name.toString();
         }
      } catch (RuntimeException | ReflectiveOperationException var5) {
      }
   }

   private static Object invokeFirst(Object target, String... methodNames) throws ReflectiveOperationException {
      ReflectiveOperationException last = null;

      for (String methodName : methodNames) {
         try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
         } catch (ReflectiveOperationException var8) {
            last = var8;
         }
      }

      throw last == null ? new NoSuchMethodException() : last;
   }

   private int fetchOnlinePlayers() {
      long now = System.currentTimeMillis();
      if (now < this.nextOnlineUpdateMillis) {
         return this.onlinePlayers;
      } else {
         this.nextOnlineUpdateMillis = now + 5000L;

         try {
            HttpRequest request = HttpRequest.newBuilder(ONLINE_ENDPOINT).timeout(Duration.ofSeconds(3L)).header("Accept", "application/json").GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
               Matcher matcher = PLAYERS_PATTERN.matcher(response.body());
               if (matcher.find()) {
                  this.onlinePlayers = Integer.parseInt(matcher.group(1));
               }
            }
         } catch (RuntimeException | IOException var6) {
         } catch (InterruptedException var7) {
            Thread.currentThread().interrupt();
         }

         return this.onlinePlayers;
      }
   }

   private static void readFrame(DiscordRpc.IpcConnection ipc) throws IOException {
      byte[] header = ipc.readFully(8);
      ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
      buffer.getInt();
      int length = buffer.getInt();
      if (length >= 0 && length <= 1048576) {
         ipc.readFully(length);
      } else {
         throw new IOException("Invalid Discord IPC frame length: " + length);
      }
   }

   private static DiscordRpc.IpcConnection connect() throws IOException {
      if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
         IOException last = null;

         for (int index = 0; index < 10; index++) {
            try {
               return DiscordRpc.IpcConnection.windows("\\\\.\\pipe\\discord-ipc-" + index);
            } catch (IOException var3) {
               last = var3;
            }
         }

         throw last == null ? new IOException("Discord IPC pipe not found") : last;
      } else {
         IOException last = null;

         for (int index = 0; index < 10; index++) {
            try {
               return DiscordRpc.IpcConnection.unix("/tmp/discord-ipc-" + index);
            } catch (IOException var4) {
               last = var4;
            }
         }

         throw last == null ? new IOException("Discord IPC socket not found") : last;
      }
   }

   private static void send(DiscordRpc.IpcConnection ipc, int opcode, String payload) throws IOException {
      byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
      ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
      header.putInt(opcode);
      header.putInt(bytes.length);
      ipc.output.write(header.array());
      ipc.output.write(bytes);
      ipc.output.flush();
   }

   private void closeConnection() {
      DiscordRpc.IpcConnection ipc = this.connection;
      this.connection = null;
      if (ipc != null) {
         try {
            ipc.close();
         } catch (IOException var3) {
         }
      }
   }

   private void sleepBeforeReconnect() {
      try {
         Thread.sleep(5000L);
      } catch (InterruptedException var2) {
         Thread.currentThread().interrupt();
      }
   }

   private static String applicationId() {
      String property = System.getProperty("freecam.discord.applicationId");
      return property != null && !property.isBlank()
         ? property.trim()
         : System.getenv().getOrDefault("FREECAM_DISCORD_APPLICATION_ID", "1553133011833323691").trim();
   }

   private static boolean isInWorld(Minecraft client) {
      return client.level != null;
   }

   private static boolean isMoonValeServer(Minecraft client) {
      ServerData server = client.getCurrentServer();
      if (server != null && server.ip != null) {
         try {
            String address = server.ip;
            String host = address.trim().toLowerCase(Locale.ROOT);
            int scheme = host.indexOf("://");
            if (scheme >= 0) {
               host = host.substring(scheme + 3);
            }

            int slash = host.indexOf(47);
            if (slash >= 0) {
               host = host.substring(0, slash);
            }

            int colon = host.indexOf(58);
            if (colon >= 0) {
               host = host.substring(0, colon);
            }

            while (host.endsWith(".")) {
               host = host.substring(0, host.length() - 1);
            }

            return host.equals("play.moonvale.ru") || host.equals("mc.moonvale.fun");
         } catch (RuntimeException var7) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean hasSurvivalLobbyStar(Minecraft client) {
      if (client.player == null) {
         return false;
      } else {
         try {
            Inventory inventory = client.player.getInventory();
            int size = Math.min(inventory.getContainerSize(), 256);

            for (int slot = 0; slot < size; slot++) {
               ItemStack stack = inventory.getItem(slot);
               if (isSurvivalLobbyStar(stack)) {
                  return true;
               }
            }
         } catch (RuntimeException var5) {
         }

         return false;
      }
   }

   private static boolean isSurvivalLobbyStar(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && stack.getItem() == Items.NETHER_STAR) {
         try {
            return "Выживание".equals(stack.getHoverName().getString().trim());
         } catch (RuntimeException var2) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static String escape(String value) {
      return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
   }

   private static final class IpcConnection implements AutoCloseable {
      private final InputStream input;
      private final OutputStream output;
      private final AutoCloseable closeable;

      private IpcConnection(InputStream input, OutputStream output, AutoCloseable closeable) {
         this.input = input;
         this.output = output;
         this.closeable = closeable;
      }

      private byte[] readFully(int length) throws IOException {
         byte[] bytes = new byte[length];
         int offset = 0;

         while (offset < length) {
            int count = this.input.read(bytes, offset, length - offset);
            if (count < 0) {
               throw new IOException("Discord IPC closed");
            }

            offset += count;
         }

         return bytes;
      }

      private static DiscordRpc.IpcConnection windows(String path) throws IOException {
         final RandomAccessFile file = new RandomAccessFile(path, "rw");
         InputStream input = new InputStream() {
            @Override
            public int read() throws IOException {
               return file.read();
            }

            @Override
            public int read(byte[] bytes, int offset, int length) throws IOException {
               return file.read(bytes, offset, length);
            }

            @Override
            public void close() throws IOException {
               file.close();
            }
         };
         OutputStream output = new OutputStream() {
            @Override
            public void write(int value) throws IOException {
               file.write(value);
            }

            @Override
            public void write(byte[] bytes, int offset, int length) throws IOException {
               file.write(bytes, offset, length);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws IOException {
               file.close();
            }
         };
         return new DiscordRpc.IpcConnection(input, output, file::close);
      }

      private static DiscordRpc.IpcConnection unix(String path) throws IOException {
         SocketChannel channel = SocketChannel.open(UnixDomainSocketAddress.of(path));
         return new DiscordRpc.IpcConnection(Channels.newInputStream(channel), Channels.newOutputStream(channel), channel::close);
      }

      @Override
      public void close() throws IOException {
         try {
            this.closeable.close();
         } catch (Exception var3) {
            if (var3 instanceof IOException ioException) {
               throw ioException;
            } else {
               throw new IOException(var3);
            }
         }
      }
   }
}
