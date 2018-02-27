package team23.tartot.network;

import team23.tartot.core.Announce;
import team23.tartot.core.Card;
import team23.tartot.core.Player;

/**
 * Created by neuracr on 20/02/18.
 */

/**
 * This interface should be implemented by the class of the core package connected to the network class.
 * The callbacks defined in this interface will be called by the network class to relay events triggered by other players (coming from the internet).
 */
public interface iNetworkToCore {
    /**
     * callback raised when an invitation is received from another player.
     */
    public void onInvitationReceived(
            //to define
    );

    /**
     * callback raised after the connection manager is triggered by a player that plays a card
     * @param player
     * @param card
     */
    public void onPlayCard(Player player, Card card);

    public void onCardsDelt(Card[] cards);

    public void onAnnounce(Player player, List<Announce> announces);

    public void onBid(Bid bid);
}
