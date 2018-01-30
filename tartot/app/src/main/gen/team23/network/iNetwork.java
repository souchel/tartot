package team23.network;
import team23.tartot.iPlayer;
import team23.tartot.iCard;

/**
 * Created by neuracr on 30/01/2018.
 */

/**
 * interface that manages the connection between players, lobby creation.
 * Everything that needs internet access.
 */
public interface iNetwork {
    public void createLobby();
    public void invitePlayer(iPlayer player);
    public void startGame();
    public void searchAutoMatchedPlayers(int nbOfSeats);
    public void leaveGame();
    public void sendMessage(iPlayer destination);
    public void playCard(iCard card);
}
