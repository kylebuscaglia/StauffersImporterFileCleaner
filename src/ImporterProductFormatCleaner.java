
public class ImporterProductFormatCleaner {

  public static void main(String[] args){
    //inFilePath, outFilePath
    if(args[0] == null || args[1] == null){
      System.out.println("Please provide both an input file path, and an output file path");
    }
    FileCleaner fc = new FileCleaner(args[0], args[1]);
    fc.cleanFile();

  }

}
