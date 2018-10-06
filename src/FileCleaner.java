import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileCleaner {

  private static final String IN_FILE_NAME = "C:\\Repos\\TestFiles\\SmallFile.txt";
  private static final String OUT_FILE_NAME = "C:\\Repos\\TestFiles\\Fixed_SmallFile.txt";
  private String[] previousColumns;
  private String[] currentColumns;
  private String[] basePriceRow;
  private ArrayList<String[]> fsRows = new ArrayList<>();
  private ArrayList<String[]> saleRows = new ArrayList<>();
  private ArrayList<String[]> tprRows = new ArrayList<>();
  private ArrayList<String[]> outRows = new ArrayList<>();

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
            combineRows(currentColumns, previousColumns, br, bw);
          } else {
            //the current row did not match the previous write out
            bw.write(String.join("|", previousColumns));
            bw.newLine();
            previousColumns = currentColumns;
          }
        }

        if (!currentColumns[0].equals(previousColumns[0])) {
          //if the last row was unique make sure its written out
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

    populateRowLists(previousColumns);
    populateRowLists(currentColumns);
    findBasePrice(br , previousColumns[0]);

    if (basePriceRow == null) {
      System.out.println("No base price found");

    } else {

      if (fsRows.size() >= saleRows.size() && fsRows.size() >= tprRows.size() ) {
        handleFsRows();
      } else if (saleRows.size() >= fsRows.size() && saleRows.size() >= tprRows.size()) {
        handleSaleRows();
      } else if (tprRows.size() > saleRows.size() &&  tprRows.size() >= fsRows.size()) {
        handleTprRows();
      } else {
        System.out.println("Should not happen..");
      }

      for(String[] outRow : outRows) {
        try {
          //write out all combined rows
          bw.write(String.join("|", outRow));
          bw.newLine();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      //remove all entries from sale type lists
      resetRows();
    }
  }

  public void resetRows() {
    outRows.clear();
    fsRows.clear();
    saleRows.clear();
    tprRows.clear();
  }

  private void findBasePrice (BufferedReader br, String productId) {

    try {
      String currentLine;
      while ((currentLine = br.readLine()) != null) {
        String[] columns = currentLine.split("\\|");

        if (columns[0].equals(productId)) {
         populateRowLists(columns);
        } else {
          this.previousColumns = columns;
          break;
        }
      }
    } catch( IOException ex) {
      ex.printStackTrace();
    }
  }

  private void populateRowLists(String[] columns) {

    if(!columns[5].equals("0") && !columns[5].equals("")) {
      basePriceRow = columns;
    }
    else if (!columns[24].equals("0") && !columns[24].equals("")) {
      fsRows.add(columns);
    }
    else if (!columns[12].equals("0") && !columns[12].equals("")) {
      saleRows.add(columns);
    }
    else if (!columns[8].equals("0") && !columns[8].equals("")) {
      tprRows.add(columns);
    }
  }

  private void handleFsRows(){
    for (String[] fsRow : fsRows) {
      String[] outRowEntry = combineFsWithBaseRow(fsRow, null);

      outRows.add(outRowEntry);
    }
    for(int i = 0; i < saleRows.size(); i++) {
      combineSaleWithBaseRow(saleRows.get(i), outRows.get(i));
    }
    for(int i = 0; i < tprRows.size(); i++) {
      combineTprWithBaseRow(tprRows.get(i), outRows.get(i));
    }
  }

  private void handleSaleRows() {

    for (String[] saleRow : saleRows) {
      String[] outRowEntry = combineSaleWithBaseRow(saleRow, null);

      outRows.add(outRowEntry);
    }
    for(int i = 0; i < fsRows.size(); i++) {
      combineFsWithBaseRow(fsRows.get(i), outRows.get(i));
    }
    for(int i = 0; i < tprRows.size(); i++) {
      combineTprWithBaseRow(tprRows.get(i), outRows.get(i));
    }
  }

  private void handleTprRows() {
    for (String[] tprRow : tprRows) {
      String[] outRowEntry = combineTprWithBaseRow(tprRow, null);

      outRows.add(outRowEntry);
    }
    for(int i = 0; i < fsRows.size(); i++) {
      combineFsWithBaseRow(fsRows.get(i), outRows.get(i));
    }
    for(int i = 0; i < saleRows.size(); i++) {
      combineSaleWithBaseRow(saleRows.get(i), outRows.get(i));
    }
  }

  private String[] combineFsWithBaseRow(String[] row, String[] outRowEntry) {

    if(outRowEntry == null) {
      outRowEntry = basePriceRow.clone();
    }
    outRowEntry[24] = row[24];
    outRowEntry[23] = row[23];
    outRowEntry[22] = row[22];

    return outRowEntry;
  }

  private String[] combineSaleWithBaseRow(String[] row, String[] outRowEntry) {

    if(outRowEntry == null) {
      outRowEntry = basePriceRow.clone();
    }

    outRowEntry[12] = row[12];
    outRowEntry[13] = row[13];
    outRowEntry[14] = row[14];
    outRowEntry[15] = row[15];

    return outRowEntry;
  }

  private String[] combineTprWithBaseRow(String[] row, String[] outRowEntry) {

    if(outRowEntry == null) {
      outRowEntry = basePriceRow.clone();
    }

    outRowEntry[8] = row[8];
    outRowEntry[9] = row[9];
    outRowEntry[10] = row[10];
    outRowEntry[11] = row[11];

    return outRowEntry;
  }
}
