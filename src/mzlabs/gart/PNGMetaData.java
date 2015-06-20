package mzlabs.gart;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.sun.imageio.plugins.png.PNGMetadata;

public class PNGMetaData {

	// from: http://java.sun.com/javase/6/docs/technotes/guides/imageio/spec/apps.fm5.html
	private static void displayMetadata(Node root) {
		displayMetadata(root, 0);
	}

	private static void indent(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("  ");
		}
	} 

	private static void displayMetadata(Node node, int level) {
		indent(level); // emit open tag
		System.out.print("<" + node.getNodeName());
		NamedNodeMap map = node.getAttributes();
		if (map != null) { // print attribute values
			int length = map.getLength();
			for (int i = 0; i < length; i++) {
				Node attr = map.item(i);
				System.out.print(" " + attr.getNodeName() +
				                 "=\"" + attr.getNodeValue() + "\"");
			}
		}

		Node child = node.getFirstChild();
		if (child != null) {
			System.out.println(">"); // close current tag
			while (child != null) { // emit child tags recursively
				displayMetadata(child, level + 1);
				child = child.getNextSibling();
			}
			indent(level); // emit close tag
			System.out.println("</" + node.getNodeName() + ">");
		} else {
			System.out.println("/>");
		}
	}

	public static void displayData(final File f) throws IOException {
		System.out.println("reading: " + f.getAbsolutePath());
		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("png");
		ImageReader reader = (ImageReader)readers.next();
		ImageInputStream iis = ImageIO.createImageInputStream(f);
		reader.setInput(iis, true);
		//BufferedImage img = reader.read(0);
		IIOMetadata md = reader.getImageMetadata(0);
		{
			Node nd = md.getAsTree(PNGMetadata.nativeMetadataFormatName);
			displayMetadata(nd);
		}
		{
			Node nd = md.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
			displayMetadata(nd);
		}
		iis.close();
	}

	public static void main(String[] args) throws IOException {
		final File inFile = new File(args[0]);
		displayData(inFile);
	}
}
