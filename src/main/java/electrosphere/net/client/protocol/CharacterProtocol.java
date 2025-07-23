package electrosphere.net.client.protocol;

import com.google.gson.Gson;

import electrosphere.client.entity.character.ClientCharacterListDTO;
import electrosphere.client.ui.menu.WindowUtils;
import electrosphere.client.ui.menu.mainmenu.MenuCharacterCreation;
import electrosphere.engine.Globals;
import electrosphere.engine.loadingthreads.LoadingThread;
import electrosphere.engine.loadingthreads.LoadingThread.LoadingThreadType;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.net.parser.net.message.CharacterMessage;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.net.template.ClientProtocolTemplate;

/**
 * The client protocol for handling character messages
 */
public class CharacterProtocol implements ClientProtocolTemplate<CharacterMessage> {

    @Override
    public CharacterMessage handleAsyncMessage(CharacterMessage message) {
        return message;
    }

    @Override
    public void handleSyncMessage(CharacterMessage message) {
        switch(message.getMessageSubtype()){
            case RESPONSECREATECHARACTERSUCCESS: {
                //trigger request to spawn character if the character list is undefined (ie if special loading case)
                Globals.clientState.clientConnection.queueOutgoingMessage(CharacterMessage.constructRequestSpawnCharacterMessage(electrosphere.net.server.protocol.CharacterProtocol.SPAWN_EXISTING_TEMPLATE + ""));
                Globals.clientState.clientConnection.queueOutgoingMessage(TerrainMessage.constructRequestMetadataMessage());
                LoadingThread clientThread = new LoadingThread(LoadingThreadType.CLIENT_WORLD);
                Globals.engineState.threadManager.start(clientThread);
            } break;
            case RESPONSECHARACTERLIST: {
                Globals.clientState.clientCharacterManager.setCharacterList(new Gson().fromJson(message.getdata(), ClientCharacterListDTO.class));
                Globals.clientState.clientCharacterManager.setWaitingOnList(false);
                Globals.engineState.signalSystem.post(SignalType.UI_MODIFICATION,() -> {
                    WindowUtils.replaceMainMenuContents(MenuCharacterCreation.createCharacterSelectionWindow());
                });
            } break;
            case REQUESTCHARACTERLIST:
            case REQUESTCREATECHARACTER:
            case REQUESTSPAWNCHARACTER:
            case RESPONSECREATECHARACTERFAILURE:
            case RESPONSESPAWNCHARACTER:
            case EDITORSWAP:
                //silently ignore
                break;
        }
    }
    
}
