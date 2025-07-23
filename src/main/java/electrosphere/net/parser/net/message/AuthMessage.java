package electrosphere.net.parser.net.message;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import electrosphere.net.parser.util.ByteStreamUtils;
import java.util.Map;
import java.util.function.BiConsumer;

public class AuthMessage extends NetworkMessage {

    /**
     * The types of messages available in this category.
     */
    public enum AuthMessageType {
        AUTHREQUEST,
        AUTHDETAILS,
        AUTHSUCCESS,
        AUTHFAILURE,
    }

    /**
     * The type of this message in particular.
     */
    AuthMessageType messageType;
    String user;
    String pass;

    /**
     * Constructor
     * @param messageType The type of this message
     */
    private AuthMessage(AuthMessageType messageType){
        this.type = MessageType.AUTH_MESSAGE;
        this.messageType = messageType;
    }

    /**
     * Constructor
     */
    protected AuthMessage(){
        this.type = MessageType.AUTH_MESSAGE;
    }

    public AuthMessageType getMessageSubtype(){
        return this.messageType;
    }

    /**
     * Gets user
     */
    public String getuser() {
        return user;
    }

    /**
     * Sets user
     */
    public void setuser(String user) {
        this.user = user;
    }

    /**
     * Gets pass
     */
    public String getpass() {
        return pass;
    }

    /**
     * Sets pass
     */
    public void setpass(String pass) {
        this.pass = pass;
    }

    /**
     * Parses a message of type AuthRequest
     */
    public static AuthMessage parseAuthRequestMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        AuthMessage rVal = (AuthMessage)pool.get(MessageType.AUTH_MESSAGE);
        rVal.messageType = AuthMessageType.AUTHREQUEST;
        return rVal;
    }

    /**
     * Constructs a message of type AuthRequest
     */
    public static AuthMessage constructAuthRequestMessage(){
        AuthMessage rVal = new AuthMessage(AuthMessageType.AUTHREQUEST);
        return rVal;
    }

    /**
     * Parses a message of type AuthDetails
     */
    public static AuthMessage parseAuthDetailsMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 8){
            return null;
        }
        int lenAccumulator = 0;
        int userlen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + userlen;
        int passlen = byteBuffer.getInt();
        lenAccumulator = lenAccumulator + passlen;
        if(byteBuffer.remaining() < 8 + lenAccumulator){
            return null;
        }
        AuthMessage rVal = (AuthMessage)pool.get(MessageType.AUTH_MESSAGE);
        rVal.messageType = AuthMessageType.AUTHDETAILS;
        if(userlen > 0){
            rVal.setuser(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, userlen));
        }
        if(passlen > 0){
            rVal.setpass(ByteStreamUtils.popStringFromByteBuffer(byteBuffer, passlen));
        }
        return rVal;
    }

    /**
     * Constructs a message of type AuthDetails
     */
    public static AuthMessage constructAuthDetailsMessage(String user,String pass){
        AuthMessage rVal = new AuthMessage(AuthMessageType.AUTHDETAILS);
        rVal.setuser(user);
        rVal.setpass(pass);
        return rVal;
    }

    /**
     * Parses a message of type AuthSuccess
     */
    public static AuthMessage parseAuthSuccessMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        AuthMessage rVal = (AuthMessage)pool.get(MessageType.AUTH_MESSAGE);
        rVal.messageType = AuthMessageType.AUTHSUCCESS;
        return rVal;
    }

    /**
     * Constructs a message of type AuthSuccess
     */
    public static AuthMessage constructAuthSuccessMessage(){
        AuthMessage rVal = new AuthMessage(AuthMessageType.AUTHSUCCESS);
        return rVal;
    }

    /**
     * Parses a message of type AuthFailure
     */
    public static AuthMessage parseAuthFailureMessage(ByteBuffer byteBuffer, MessagePool pool, Map<Short,BiConsumer<NetworkMessage,ByteBuffer>> customParserMap){
        if(byteBuffer.remaining() < 0){
            return null;
        }
        AuthMessage rVal = (AuthMessage)pool.get(MessageType.AUTH_MESSAGE);
        rVal.messageType = AuthMessageType.AUTHFAILURE;
        return rVal;
    }

    /**
     * Constructs a message of type AuthFailure
     */
    public static AuthMessage constructAuthFailureMessage(){
        AuthMessage rVal = new AuthMessage(AuthMessageType.AUTHFAILURE);
        return rVal;
    }

    @Deprecated
    @Override
    void serialize(){
        byte[] intValues = new byte[8];
        byte[] stringBytes;
        switch(this.messageType){
            case AUTHREQUEST:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_AUTH;
                //entity messaage header
                rawBytes[1] = TypeBytes.AUTH_MESSAGE_TYPE_AUTHREQUEST;
                break;
            case AUTHDETAILS:
                rawBytes = new byte[2+4+user.length()+4+pass.length()];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_AUTH;
                //entity messaage header
                rawBytes[1] = TypeBytes.AUTH_MESSAGE_TYPE_AUTHDETAILS;
                intValues = ByteStreamUtils.serializeIntToBytes(user.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[2+i] = intValues[i];
                }
                stringBytes = user.getBytes();
                for(int i = 0; i < user.length(); i++){
                    rawBytes[6+i] = stringBytes[i];
                }
                intValues = ByteStreamUtils.serializeIntToBytes(pass.length());
                for(int i = 0; i < 4; i++){
                    rawBytes[6+user.length()+i] = intValues[i];
                }
                stringBytes = pass.getBytes();
                for(int i = 0; i < pass.length(); i++){
                    rawBytes[10+user.length()+i] = stringBytes[i];
                }
                break;
            case AUTHSUCCESS:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_AUTH;
                //entity messaage header
                rawBytes[1] = TypeBytes.AUTH_MESSAGE_TYPE_AUTHSUCCESS;
                break;
            case AUTHFAILURE:
                rawBytes = new byte[2];
                //message header
                rawBytes[0] = TypeBytes.MESSAGE_TYPE_AUTH;
                //entity messaage header
                rawBytes[1] = TypeBytes.AUTH_MESSAGE_TYPE_AUTHFAILURE;
                break;
        }
        serialized = true;
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        switch(this.messageType){
            case AUTHREQUEST: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_AUTH);
                stream.write(TypeBytes.AUTH_MESSAGE_TYPE_AUTHREQUEST);
                
                //
                //Write body of packet
            } break;
            case AUTHDETAILS: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_AUTH);
                stream.write(TypeBytes.AUTH_MESSAGE_TYPE_AUTHDETAILS);
                
                //
                //Write variable length table in packet
                ByteStreamUtils.writeInt(stream, user.getBytes().length);
                ByteStreamUtils.writeInt(stream, pass.getBytes().length);
                
                //
                //Write body of packet
                ByteStreamUtils.writeString(stream, user);
                ByteStreamUtils.writeString(stream, pass);
            } break;
            case AUTHSUCCESS: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_AUTH);
                stream.write(TypeBytes.AUTH_MESSAGE_TYPE_AUTHSUCCESS);
                
                //
                //Write body of packet
            } break;
            case AUTHFAILURE: {
                
                //
                //message header
                stream.write(TypeBytes.MESSAGE_TYPE_AUTH);
                stream.write(TypeBytes.AUTH_MESSAGE_TYPE_AUTHFAILURE);
                
                //
                //Write body of packet
            } break;
        }
    }

}
