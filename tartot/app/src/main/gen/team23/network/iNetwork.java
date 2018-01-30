package team23.network;
import java.util.List;

import team23.tartot.iPlayer;
import team23.tartot.iCard;

/**
 * Created by neuracr on 30/01/2018.
 * iNetwork interface definition v0.1
 */

/**
 * interface that manages the connection between players, lobby creation.
 * Everything that needs internet access.
 */
public interface iNetwork {
    /**
     * creates a new empty lobby
     */
    public void createLobby();

    /**
     * Leaves the current lobby. If we were lobby leader, assign leader role
     * to a new player in lobby. Deletes the lobby otherwise.
     */
    public void leaveLobby();

    /**
     * get the list a the friends of the player
     * @return list of friends
     */
    public List<iPlayer> getFriendsList();

    /**
     * Add a player to the friends
     * @param newFriend
     */
    public void addToFriends(iPlayer newFriend);

    /**
     * Invite the player in parameter in the lobby occupied by us.
     * @param player the player to invite
     */
    public void inviteFriend(iPlayer player);

    /**
     * Start a new game with the players in the lobby.
     */
    public void startGame();

    /**
     * Leave the current game and notify the other players
     */
    public void leaveGame();

    /**
     * callback to treat incomming data when in game (card played, announce, ...)
     * @param data
     */
    public void onInGameDataReceive(Object data);

    /**
     * callback to treat incomming data when in lobby (player left the lobby, game starts, ...)
     */
    public void onInLobbyDataReceive();

    /**
     * notify other players the card we chosed to play
     * @param card
     */
    public void playCard(iCard card);
}
