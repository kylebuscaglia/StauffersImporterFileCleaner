import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileCleaner {

  private static final String IN_FILE_NAME = "C:\\Repos\\TestFiles\\in_201810030301_cswg_Stauffers54665_sw_prices.txt";
  private static final String OUT_FILE_NAME = "C:\\Repos\\TestFiles\\Fixed_201810030301_cswg_Stauffers54665_sw_prices.txt";
  private String[] outColumns = new String[25];
  private String[] previousColumns;
  private String[] currentColumns;

  public void cleanFile(){
    try (BufferedReader br = new BufferedReader(new FileReader(IN_FILE_NAME))) {

      String currentLine;

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(OUT_FILE_NAME, true))) {

        while ((currentLine = br.readLine()) != null) {

          currentColumns = currentLine.split("\\|");
          //handle base case/ first line
          if (previousColumns == null) {
            previousColumns = currentColumns;
            continue;
          }

          if (currentColumns[0].equals(previousColumns[0])) {
            //set the outColumns to current so that outColumns that are not over written in
            //copyNeededColumns are still filled in
            outColumns = currentColumns;
            combineRows(currentColumns, previousColumns, br, bw);
          } else {
            //the current row did not match the previous write out
            bw.write(String.join("|", previousColumns));
            bw.newLine();
            previousColumns = currentColumns;
          }

        }

        if (outColumns[0].equals(previousColumns[0])) {
          //if the last rows were all the same product
          bw.write(String.join("|", outColumns));
        } else {
          //if the last row was a unique product
          bw.write(String.join("|", previousColumns));
        }
        bw.newLine();
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   *
   * @param currentColumns
   * @param previousColumns
   * @param br
   * @param bw
   */
  private void combineRows(String[] currentColumns,
                           String[] previousColumns,
                           BufferedReader br,
                           BufferedWriter bw) {

    copyNeededColumns(currentColumns);
    copyNeededColumns(previousColumns);

    try {
      String currentLine;

      if ((currentLine = br.readLine()) != null) {
        String[] nextColumns = currentLine.split("\\|");
        if ( nextColumns[0].equals(currentColumns[0])) {
          //continue to combine rows and read the next line until
          //we get to a new product
          combineRows(previousColumns, nextColumns, br, bw);
        } else {
          //current line is now for a new product, write out the result of combining the rows
          bw.write(String.join("|", outColumns));
          bw.newLine();
          //adjust the previous columns to keep them in sync with outer loop
          this.currentColumns = previousColumns;
          this.previousColumns = nextColumns;
        }
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Use the quantity column to determine the type of row that is being processed
   *
   * @param columns
   */
  private void copyNeededColumns(String[] columns) {
    //Base Price Row
    if(!columns[5].equals("0") && !columns[5].equals("")) {
      outColumns[5] = columns[5];
      outColumns[6] = columns[6];
      outColumns[7] = columns[7];
    }
    //FS Pricing Row
    if (!columns[24].equals("0") && !columns[24].equals("")) {
      outColumns[24] = columns[24];
      outColumns[23] = columns[23];
      outColumns[22] = columns[22];
    }
    //Sale Price Row
    if (!columns[12].equals("0") && !columns[12].equals("")) {
      outColumns[12] = columns[12];
      outColumns[13] = columns[13];
      outColumns[14] = columns[14];
      outColumns[15] = columns[15];
    }
    //TPR Row
    if (!columns[8].equals("0") && !columns[8].equals("")) {
      outColumns[8] = columns[8];
      outColumns[9] = columns[9];
      outColumns[10] = columns[10];
      outColumns[11] = columns[11];
    }

  }

}
