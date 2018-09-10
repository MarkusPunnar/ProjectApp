package byteMe.model;

public class AssessmentElement {

    private int elementId;
    private String elementContent;

    public AssessmentElement(int elementId, String elementContent) {
        this.elementId = elementId;
        this.elementContent = elementContent;
    }

    public String getElementContent() {
        return elementContent;
    }

    public void setElementContent(String elementContent) {
        this.elementContent = elementContent;
    }

    public int getElementId() {
        return elementId;
    }

    public void setElementId(int elementId) {
        this.elementId = elementId;
    }

}