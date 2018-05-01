package team23.tartot.core;

import java.util.ArrayList;

public interface callbackGameManager {
	//TODO wtf à quoi sert cette class finalement?
	public void askBid(Bid bid);
	public void askAnnounce(ArrayList<Announces> announce);
	public void onReceivedPosition(int position);
	public void ecarter(Card[] cards);
	void startNextFoldCalledBack(Card card);
	void continuFoldCalledBack(Card card);
}
