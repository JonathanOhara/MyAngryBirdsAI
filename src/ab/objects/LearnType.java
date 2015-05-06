package ab.objects;

public enum LearnType {
	None("none"),
	ConfirmBestResults("confirmBestResults"),
	RounRobin("roundRobin"),
	AllShots("allShots"),
	Random("random");
	
	private String text;
	
	private LearnType(String text) {
		this.text = text;
	}
	
	public String getText() {
		return this.text;
	}

	public static LearnType fromString(String text) {
		if (text != null) {
			for (LearnType b : LearnType.values()) {
				if (text.equalsIgnoreCase(b.text)) {
					return b;
				}
			}
		}
		return null;
	}
}
