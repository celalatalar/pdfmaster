package PdfMaster;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;
import com.lowagie.text.pdf.SimpleNamedDestination;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

public class PdfCore
{
  public static int AramaAralikBoyutu = 10;
  public static long AramaZamani;

  public static int Arama(String PdfPath, String Aranacak)
    throws IOException
  {   
            
    Date bas = new Date();
    PDDocument doc = null;
    int sayfa = -1;
    if (PdfPath == null) {
      return -1;
    }
    
    File f = new File(PdfPath);
    if (!f.exists()) {
      return -1;
    }
    
    doc = PDDocument.load(PdfPath);

    int sayfasayisi = doc.getNumberOfPages();
    
            
    PDFTextStripper stripper = new PDFTextStripper();
    for (int a = 1; a < sayfasayisi; a += AramaAralikBoyutu)
    {
      stripper.setStartPage(a);
      if (a + AramaAralikBoyutu - 1 <= sayfasayisi) {
        stripper.setEndPage(a + AramaAralikBoyutu - 1);
      } else {
        stripper.setEndPage(sayfasayisi);
      }
      String tut = stripper.getText(doc).toUpperCase();

      int k = tut.indexOf(Aranacak.toUpperCase());
      if (k > -1)
      {
        sayfa = a;
        break;
      }
    }
    doc.close();
    Date son = new Date();
    AramaZamani = son.getTime() - bas.getTime();

    return sayfa;
  }

  public static int HizliArama1(String PdfPath, String Aranacak)
    throws IOException
  {
    Date bas = new Date();
    int sayfa = -1;
    PdfReader okuyucu = new PdfReader(PdfPath);
    HashMap bmap = SimpleNamedDestination.getNamedDestination(okuyucu, false);
    List list = SimpleBookmark.getBookmark(okuyucu);
    for (Iterator i = list.iterator(); i.hasNext();)
    {
      String s = i.next().toString();
      int konum = s.indexOf(Aranacak);
      if (konum != -1)
      {
        char[] yazi = s.toCharArray();
        int parantezSayisi = 1;
        int ii = konum;
        for (; ii >= 0; ii--)
        {
          if (yazi[ii] == '}') {
            parantezSayisi++;
          } else if (yazi[ii] == '{') {
            parantezSayisi--;
          }
          if ((yazi[ii] == '{') && (parantezSayisi == 0)) {
            break;
          }
        }
        int basKonum = s.indexOf("Named=", ii);
        if (basKonum != -1)
        {
          int virgulKonum = s.indexOf(",", basKonum);
          String named = s.substring(basKonum + 6, virgulKonum);
          String bilgi = bmap.get(named).toString();
          sayfa = Integer.parseInt(bilgi.substring(0, bilgi.indexOf(" ")));
        }
        else
        {
          basKonum = s.indexOf("Page=", ii);
          sayfa = Integer.parseInt(s.substring(basKonum + 5, s.indexOf(" ", basKonum + 5)));
        }
      }
    }
    okuyucu.close();

    Date son = new Date();
    AramaZamani = son.getTime() - bas.getTime();

    return sayfa;
  }

  public static int HizliArama(String PdfPath, String Aranacak)
    throws IOException
  {
    Date bas = new Date();
    if (PdfPath == null) {
      return -1;
    }
    int sayfa = -1;
    int pbas = -1;int pabas = -1;int pason = -1;int pson = -1;
    File f = new File(PdfPath);
    if (!f.exists()) {
      return -1;
    }
    PdfReader okuyucu = new PdfReader(PdfPath);
    if (okuyucu == null) {
      return -1;
    }
    HashMap bmap = SimpleNamedDestination.getNamedDestination(okuyucu, false);
    List list = SimpleBookmark.getBookmark(okuyucu);
    if (list == null) {
      return sayfa;
    }
    for (Iterator i = list.iterator(); i.hasNext();)
    {
      String s = i.next().toString().toUpperCase();
      int konum = s.indexOf(Aranacak.toUpperCase());
      if (konum != -1)
      {
        char[] yazi = s.toCharArray();
        int parantezSayisi = 1;
        int ii = konum;
        for (; ii >= 0; ii--) {
          if (yazi[ii] == ']')
          {
            pason = ii--;
            for (; (yazi[ii] != '[') || (parantezSayisi != 1); ii--) {
              if (yazi[ii] == ']') {
                parantezSayisi++;
              } else if (yazi[ii] == '[') {
                parantezSayisi--;
              }
            }
            pabas = ii;
          }
          else if (yazi[ii] == '{')
          {
            pbas = ii;
            break;
          }
        }
        ii = konum;
        for (;ii < s.length(); ii++) {
          if (yazi[ii] == '[')
          {
            parantezSayisi = 1;
            pabas = ii++;
            for (; (yazi[ii] != ']') || (parantezSayisi != 1); ii--) {
              if (yazi[ii] == ']') {
                parantezSayisi++;
              } else if (yazi[ii] == '[') {
                parantezSayisi--;
              }
            }
            pason = ii;
          }
          else if (yazi[ii] == '}')
          {
            pson = ii;
            break;
          }
        }
        String kesilmis;
        if (pabas == -1) {
          kesilmis = s.substring(pbas, pson);
        } else {
          kesilmis = s.substring(pbas, pabas + 1) + s.substring(pason, pson + 1);
        }
        konum = kesilmis.indexOf("NAMED=");
        if (konum == -1)
        {
          konum = kesilmis.indexOf("PAGE=");
          sayfa = Integer.parseInt(kesilmis.substring(konum + 5, kesilmis.indexOf(" ", konum + 5)));
        }
        else
        {
          String named = kesilmis.substring(konum + 6, kesilmis.indexOf(",", konum + 6));
          String info = bmap.get(named).toString();
          sayfa = Integer.parseInt(info.substring(0, info.indexOf(" ")));
        }
      }
    }
    okuyucu.close();
    Date son = new Date();
    AramaZamani = son.getTime() - bas.getTime();
    return sayfa;
  }

  public static ByteArrayOutputStream Goruntule(String id, String bas, String son)
    throws SQLException, IOException, ClassNotFoundException, DocumentException, InstantiationException, IllegalAccessException
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection conTest = DriverManager.getConnection("jdbc:mysql://localhost/pdf", "root", "1");
    Statement komut = conTest.createStatement();

    ResultSet rs = komut.executeQuery("SELECT path,name FROM pdf WHERE id=" + String.valueOf(id));
    if (!rs.next()) {
      return null;
    }
    PdfReader reader = new PdfReader(rs.getString("path"));
    Document document = new Document();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PdfCopy copy = new PdfCopy(document, baos);
    int sonn = Integer.parseInt(son);
    if (reader.getNumberOfPages() + 1 < sonn) {
      sonn = reader.getNumberOfPages() + 1;
    }
    document.open();
    for (int i = Integer.parseInt(bas); i < sonn; i++) {
      copy.addPage(copy.getImportedPage(reader, i));
    }
    copy.setViewerPreferences(256);
    document.close();

    return baos;
  }

  public static ByteArrayOutputStream Raporla(ResultSet rs)
  {
    Document doc = new Document();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try
    {
      PdfWriter.getInstance(doc, baos);

      ResultSetMetaData rsmd = rs.getMetaData();
      int numColumns = rsmd.getColumnCount();
      PdfPTable table = new PdfPTable(numColumns);
      for (int i = 1; i <= numColumns; i++) {
        table.addCell(rsmd.getColumnName(i));
      }
      while (rs.previous()) {}
      while (rs.next()) {
        for (int i = 1; i <= numColumns; i++)
        {
          String columnName = rsmd.getColumnName(i);

          PdfPCell cell = new PdfPCell(new Paragraph(rs.getString(columnName)));
          table.addCell(cell);
        }
      }
      doc.open();
      doc.add(table);
      doc.close();
    }
    catch (SQLException ex)
    {
      Logger.getLogger(PdfCore.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DocumentException ex)
    {
      Logger.getLogger(PdfCore.class.getName()).log(Level.SEVERE, null, ex);
    }
    return baos;
  }
}
