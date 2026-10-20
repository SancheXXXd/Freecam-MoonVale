package net.xolt.freecam.tripod;

public enum TripodSlot {
   NONE,
   ONE,
   TWO,
   THREE,
   FOUR,
   FIVE,
   SIX,
   SEVEN,
   EIGHT,
   NINE;

   public static final int MIN = 1;
   public static final int MAX = values().length - 1;

   @Override
   public String toString() {
      return this == NONE ? "None" : "#%d".formatted(this.ordinal());
   }

   public static TripodSlot valueOf(int number) throws IndexOutOfBoundsException {
      if (number >= 1 && number <= MAX) {
         return values()[number];
      } else {
         throw new IndexOutOfBoundsException("Cannot get TripodSlot for number %d: must be %d-%d.".formatted(number, 1, MAX));
      }
   }

   public static TripodSlot ofKeyCode(int keyCode) {
      return switch (keyCode) {
         case 30 -> ONE;
         case 31 -> TWO;
         case 32 -> THREE;
         case 33 -> FOUR;
         case 34 -> FIVE;
         case 35 -> SIX;
         case 36 -> SEVEN;
         case 37 -> EIGHT;
         case 38 -> NINE;
         default -> NONE;
      };
   }
}
