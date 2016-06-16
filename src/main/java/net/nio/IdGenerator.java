package net.nio;

public class IdGenerator {

	private static final long MAX_VALUE = Long.MAX_VALUE;
	private static IdGenerator instance=new IdGenerator();
    public static IdGenerator getINSTNCE()
    {
    	return instance;
    }
	private  long connectId = 0L;
	private final Object lock = new Object();

	public long getId() {
		synchronized (lock) {
			if (connectId >= MAX_VALUE) {
				connectId = 0L;
			}
			return ++connectId;
		}
	}
	
	
}