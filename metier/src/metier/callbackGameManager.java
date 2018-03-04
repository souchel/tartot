package metier;

public interface callbackGameManager {
	public void askBid(Bid bid);
	public void askAnnounce(Announces announce);
	void onReceivedPosition(int position);
}
