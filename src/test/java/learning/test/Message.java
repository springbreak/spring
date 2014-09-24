package learning.test;

public class Message {
	private String text;

	private Message(String text) {
		this.text = text;
	}
	
	public String getMessage() {
		return this.text;
	}
	
	static public Message getMessage(String text) {
		return new Message(text);
	}
}
