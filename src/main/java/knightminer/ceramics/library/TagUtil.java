package knightminer.ceramics.library;

public class TagUtil {

	public static byte[] toByteArray(boolean[] bools) {
		byte[] bytes = new byte[bools.length];
		for(int i = 0; i < bools.length; i++) {
			bytes[i] = (byte) (bools[i] ? 1 : 0);
		}
		return bytes;
	}

	public static boolean[] toBoolArray(byte[] bytes) {
		boolean[] bools = new boolean[bytes.length];
		for(int i = 0; i < bytes.length; i++) {
			bools[i] = bytes[i] != 0;
		}
		return bools;
	}
}
