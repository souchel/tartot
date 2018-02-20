package team23.tartot.network;

import team23.tartot.core.Announce;
import team23.tartot.core.Card;
import team23.tartot.core.Player;
import team23.tartot.core.iPlayer;

/**
 * Created by thomas on 2/20/18.
 */

public class APIManager implements iCoreToNetwork {
    /**
     * creates a new empty lobby
     */
    public void createLobby() {

    }

    /**
     * Leaves the current lobby. If we were lobby leader, assign leader role
     * to a new player in lobby. Deletes the lobby otherwise.
     */
    public void leaveLobby();


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
     * deal the cards to the other players.
     * Raise an exception if the player is not in game.
     * No other verification is done.
     * @param destination: the player we send the cards to
     * @param cards: the cards delt
     */
    public void dealCards(Player destination, Card[] cards);

    /**
     * notify other players the card we chosed to play
     * @param card
     */
    public void playCard(Card card);

    /**
     * Inform other players of an announce. Raise an exception if we are not in game
     * @param announce: the announce to broadcast
     */
    public void announce(Announce announce);
}
