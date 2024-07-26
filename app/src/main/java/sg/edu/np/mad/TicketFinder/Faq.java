package sg.edu.np.mad.TicketFinder;

import java.util.List;

public class Faq {
    private String Question;
    private String Answer;
    private List<String> Keywords;

    public Faq() {
        // Default constructor required for calls to DataSnapshot.getValue(Faq.class)
    }

    public Faq(String Question, String Answer, List<String> Keywords) {
        this.Question = Question;
        this.Answer = Answer;
        this.Keywords = Keywords;
    }

    // Getter for Question
    public String getQuestion() {
        return Question;
    }

    // Setter for Question
    public void setQuestion(String Question) {
        this.Question = Question;
    }

    // Getter for Answer
    public String getAnswer() {
        return Answer;
    }

    // Setter for Answer
    public void setAnswer(String Answer) {
        this.Answer = Answer;
    }

    // Getter for Keywords
    public List<String> getKeywords() {
        return Keywords;
    }

    // Setter for Keywords
    public void setKeywords(List<String> Keywords) {
        this.Keywords = Keywords;
    }
}
