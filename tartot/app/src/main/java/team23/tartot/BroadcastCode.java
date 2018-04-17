package team23.tartot;

public enum BroadcastCode {
    ERROR,

    //broadcasts for the GameService->GameActivity
    STATE_UPDATE, //when the state changed and the UI has to update some information (players online, state of players, ...)
    READY_TO_START,


    //broadcasts for the ApiManagerService->GameService
    CARD_RECEIVED,

    //broadcasts from the ApiManagerService
    MANUAL_LOG, //when the automatic connection does not work. We ask the player to manually connect to his account
    CONNECTED_TO_GOOGLE, //when we have successfully logged in the player
    KEEP_SCREEN_ON, //ask the activity not to turn of the screen
    ROOM_CREATED, //call back when the room is successfully created
    ROOM_JOINED, //callback when we joined a room
    ROOM_LEFT,
    ROOM_CONNECTED, //Called when all the participants in a real-time room are fully connected
    SHOW_WAITING_ROOM,
    INVITATION_RECEIVED,
    INVITATION_REMOVED,
    SHOW_PLAYER_PICKER,

    //test
    EXAMPLE,

    ;

}
