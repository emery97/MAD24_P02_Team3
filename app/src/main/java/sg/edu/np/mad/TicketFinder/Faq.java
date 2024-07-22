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

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String Question) {
        this.Question = Question;
    }

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String Answer) {
        this.Answer = Answer;
    }

    public List<String> getKeywords() {
        return Keywords;
    }

    public void setKeywords(List<String> Keywords) {
        this.Keywords = Keywords;
    }
}
