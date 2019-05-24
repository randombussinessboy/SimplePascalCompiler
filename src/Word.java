
/*
该POJO类用来存储词法分析后的结构化单词
包含该单词的内容，类型，坐标
 */
public class Word {
    private String content;
    private int type;
    private int wordRow;
    private int wordColumn;

    public Word(String content, int type, int wordRow, int wordColumn) {
        this.content = content;
        this.type = type;
        this.wordRow = wordRow;
        this.wordColumn = wordColumn;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getWordRow() {
        return wordRow;
    }

    public void setWordRow(int wordRow) {
        this.wordRow = wordRow;
    }

    public int getWordColumn() {
        return wordColumn;
    }

    public void setWordColumn(int wordColumn) {
        this.wordColumn = wordColumn;
    }
}
