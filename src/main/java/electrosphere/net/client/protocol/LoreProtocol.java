package electrosphere.net.client.protocol;

import java.util.List;

import com.google.gson.Gson;

import electrosphere.data.macro.temporal.MacroTemporalData;
import electrosphere.engine.Globals;
import electrosphere.net.parser.net.message.LoreMessage;
import electrosphere.net.template.ClientProtocolTemplate;
import electrosphere.util.SerializationUtils;

public class LoreProtocol implements ClientProtocolTemplate<LoreMessage> {

    @Override
    public LoreMessage handleAsyncMessage(LoreMessage message) {
        return message;
    }

    @Override
    public void handleSyncMessage(LoreMessage message) {
        switch(message.getMessageSubtype()){
            case RESPONSERACES: {
                //we get back the race list as a json array, deserialize, and push into type loader
                @SuppressWarnings("unchecked")
                List<String> playableRaces = new Gson().fromJson(message.getdata(), List.class);
                Globals.gameConfigCurrent.getCreatureTypeLoader().loadPlayableRaces(playableRaces);
            } break;
            case TEMPORALUPDATE: {
                MacroTemporalData temporalData = SerializationUtils.deserialize(message.getdata(), MacroTemporalData.class);
                Globals.clientState.clientTemporalService.setLatestData(temporalData);
            } break;
            
            case REQUESTRACES: {
                //silently ignore
            } break;
        }
    }

}
