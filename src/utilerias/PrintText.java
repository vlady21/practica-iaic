package utilerias;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.text.*;

/**
 *
 * @author Victor
 */
public class PrintText implements Printable {
  
  private static AttributedString mStyledText;
  
  static public void imprimir(String mText) throws PrinterException {

      mStyledText = new AttributedString(mText);

      PrinterJob printerJob = PrinterJob.getPrinterJob();

      Book book = new Book();

      book.append(new PrintText(), new PageFormat());

      printerJob.setPageable(book);

      boolean doPrint = printerJob.printDialog();

      if (doPrint) {

          printerJob.print();

      }
  }
  
  public int print(Graphics g, PageFormat format, int pageIndex) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.translate(format.getImageableX(), format.getImageableY());

        g2d.setPaint(Color.black);

        Point2D.Float pen = new Point2D.Float();
        AttributedCharacterIterator charIterator = mStyledText.getIterator();
        LineBreakMeasurer measurer = new LineBreakMeasurer(charIterator,
        g2d.getFontRenderContext());
        float wrappingWidth = (float) format.getImageableWidth();
        while (measurer.getPosition() < charIterator.getEndIndex()) {
          TextLayout layout = measurer.nextLayout(wrappingWidth);
          pen.y += layout.getAscent();
          float dx = layout.isLeftToRight()? 0 :
                     (wrappingWidth - layout.getAdvance());
          layout.draw(g2d, pen.x + dx, pen.y);
          pen.y += layout.getDescent() + layout.getLeading();
        }

        return Printable.PAGE_EXISTS;
  }
}
