import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileCleaner {

  private String inFilePath;
  private String outFilePath;
  private String[] previousColumns;
  private String[] currentColumns;
  private String[] basePriceRow;
  private ArrayList<String[]> fsRows = new ArrayList<>();
  private ArrayList<String[]> saleRows = new ArrayList<>();
  private ArrayList<String[]> tprRows = new ArrayList<>();
  private ArrayList<String[]> outRows = new ArrayList<>();

  public FileCleaner(String inFilePath, String outFilePath) {
    this.inFilePath = inFilePath;
    this.outFilePath = outFilePath;
  }

  public void cleanFile(){
    try (BufferedReader br = new BufferedReader(new FileReader(inFilePath))) {

      String currentLine;

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(outFilePath, true))) {
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
          bw.newLine();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles writing out of combined data rows
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

      //Determine which price type had the most corresponding rows
      //So we know how many total rows we will need to write out for this product
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

  /**
   *  TODO: rename this method
   * Loops through all of the rows for a single product and sorts each row by its price type
   *
   * @param br
   * @param productId
   */
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

  /**
   * Determines the type of row based on the contents of specific columns (base, fs, sale, tpr)
   * and adds the row to the corresponding list to be processed
   * @param row the columns for the row being processed
   */
  private void populateRowLists(String[] row) {

    if(!row[5].equals("0") && !row[5].equals("")) {
      basePriceRow = row;
    }
    else if (!row[24].equals("0") && !row[24].equals("")) {
      fsRows.add(row);
    }
    else if (!row[12].equals("0") && !row[12].equals("")) {
      saleRows.add(row);
    }
    else if (!row[8].equals("0") && !row[8].equals("")) {
      tprRows.add(row);
    }
  }

  /**
   * When there are more fs rows than any other type for a product, create an outRow entry
   * for each fs row, ex 3 fs prices for one product means we need to write out 3 rows total for
   * this product.
   *
   * write the sale and tpr data to the number of applicable rows
   */
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

  /**
   * When there are more sale rows than any other type for a product, create an outRow entry
   * for each sale row, ex 3 sale prices for one product means we need to write out 3 rows total for
   * this product.
   *
   * write the fs and tpr data to the number of applicable rows
   */
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

  /**
   *
   * @param row the row containing the fs data
   * @param outRowEntry row containing the base price data and any other already combined rows
   * @return a row containing the previously combined data with the new fs data
   */
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
