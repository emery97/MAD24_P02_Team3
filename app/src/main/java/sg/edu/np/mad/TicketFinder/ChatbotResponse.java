package sg.edu.np.mad.TicketFinder;

public class ChatbotResponse {
    private String Question;
    private String Answer;

    // Default constructor is required for calls to DataSnapshot.getValue(ChatbotResponse.class)
    public ChatbotResponse() {
    }

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String question) {
        Question = question;
    }

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }
}
