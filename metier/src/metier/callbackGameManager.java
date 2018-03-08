package metier;

public interface callbackGameManager {
	public void askBid(Bid bid);
	public void askAnnounce(Announces announce);
	public void onReceivedPosition(int position);
	public void ecarter(Card[] cards);
	void startNectFoldCalledBack(Card card);
	void continuFoldCalledBack(Card card);
}
