package common;

public class Message {
    public String type;
    public String content;

    // ------- Constructors -------

    public Message(){
        this.type = "";
        this.content = "";
    }

    public Message(String type, String content){
        this.type = type;
        this.content = content;
    }

    // ------- Encode and Decode -------


    public String encode(){
        return type + "|" + content;
    }

    public void decode(String encoded){
        String[] parts = encoded.split("\\|", 2); // split into 2 parts only
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid message format: " + encoded);
        }
        type = parts[0];
        content = parts[1];
    }

    public static boolean isValidFormat(String encoded){
        String[] parts = encoded.split("\\|", 2);
        return parts.length >= 2;
    }


    // ------- Getters and Setters -------

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
