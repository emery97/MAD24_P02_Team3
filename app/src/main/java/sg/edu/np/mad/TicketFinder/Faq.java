package sg.edu.np.mad.TicketFinder;

import java.util.List;

public class Faq {
    private String question;
    private String answer;
    private List<String> keywords;

    public Faq() {
        // Default constructor required for calls to DataSnapshot.getValue(Faq.class)
    }

    public Faq(String question, String answer, List<String> keywords) {
        this.question = question;
        this.answer = answer;
        this.keywords = keywords;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
