package com.gpl.rpg.AndorsTrail.model.map;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.xmlpull.v1.XmlPullParserException;

import com.gpl.rpg.AndorsTrail.util.Base64;
import com.gpl.rpg.AndorsTrail.util.L;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;

public final class TMXMapFileParser {
	public static TMXMap read(Resources r, int xmlResourceId, String name) {
		return read(r.getXml(xmlResourceId), xmlResourceId, name);
	}
	
	public static TMXLayerMap readLayeredTileMap(Resources r, int xmlResourceId, String name) {
		return readLayerMap(r.getXml(xmlResourceId), name);
	}
	
	private static TMXMap read(XmlResourceParser xrp, int xmlResourceId, String name) {
		final TMXMap map = new TMXMap();
		map.xmlResourceId = xmlResourceId;
		try {
			// Map format: http://sourceforge.net/apps/mediawiki/tiled/index.php?title=Examining_the_map_format
			int eventType;
			final ArrayList<TMXLayer> layers = new ArrayList<TMXLayer>();
			final ArrayList<TMXTileSet> tileSets = new ArrayList<TMXTileSet>();
			while ((eventType = xrp.next()) != XmlResourceParser.END_DOCUMENT) {
				if (eventType == XmlResourceParser.START_TAG) {
					String s = xrp.getName();
					if (s.equals("map")) {
						map.name = name;
						map.orientation = xrp.getAttributeValue(null, "orientation");
						map.width = xrp.getAttributeIntValue(null, "width", -1);
						map.height = xrp.getAttributeIntValue(null, "height", -1);
						map.tilewidth = xrp.getAttributeIntValue(null, "tilewidth", -1);
						map.tileheight = xrp.getAttributeIntValue(null, "tileheight", -1);
						readCurrentTagUntilEnd(xrp, new TagHandler() {
							public void handleTag(XmlResourceParser xrp, String tagName) throws XmlPullParserException, IOException {
								if (tagName.equals("tileset")) {
									tileSets.add(readTMXTileSet(xrp));
								} else if (tagName.equals("objectgroup")) {
									map.objectGroups.add(readTMXObjectGroup(xrp));
								} else if (tagName.equals("layer")) {
									layers.add(readTMXMapLayer(xrp));
								}
							}
						});
					} 
				}
            }
            xrp.close();
            map.layers = layers.toArray(new TMXLayer[layers.size()]);
            map.tileSets = tileSets.toArray(new TMXTileSet[tileSets.size()]);
		} catch (XmlPullParserException e) {
			L.log("Error reading map \"" + name + "\": XmlPullParserException : " + e.toString());
		} catch (IOException e) {
			L.log("Error reading map \"" + name + "\": IOException : " + e.toString());
		}
		return map;
	}
	

	private static TMXLayerMap readLayerMap(XmlResourceParser xrp, final String name) {
		TMXLayerMap map = new TMXLayerMap();
		try {
			int eventType;
			final ArrayList<TMXLayer> layers = new ArrayList<TMXLayer>();
			final ArrayList<TMXTileSet> tileSets = new ArrayList<TMXTileSet>();
			while ((eventType = xrp.next()) != XmlResourceParser.END_DOCUMENT) {
				if (eventType == XmlResourceParser.START_TAG) {
					String s = xrp.getName();
					if (s.equals("map")) {
						map.width = xrp.getAttributeIntValue(null, "width", -1);
						map.height = xrp.getAttributeIntValue(null, "height", -1);
						readCurrentTagUntilEnd(xrp, new TagHandler() {
							public void handleTag(XmlResourceParser xrp, String tagName) throws XmlPullParserException, IOException {
								if (tagName.equals("tileset")) {
									tileSets.add(readTMXTileSet(xrp));
								} else if (tagName.equals("layer")) {
									layers.add(readTMXMapLayer(xrp));
								}
							}
						});
					} 
				}
            }
            xrp.close();
            map.layers = layers.toArray(new TMXLayer[layers.size()]);
            map.tileSets = tileSets.toArray(new TMXTileSet[tileSets.size()]);
		} catch (XmlPullParserException e) {
			L.log("Error reading layered map \"" + name + "\": XmlPullParserException : " + e.toString());
		} catch (IOException e) {
			L.log("Error reading layered map \"" + name + "\": IOException : " + e.toString());
		}
		return map;
	}


	private static TMXTileSet readTMXTileSet(XmlResourceParser xrp) throws XmlPullParserException, IOException {
		final TMXTileSet ts = new TMXTileSet();
		ts.firstgid = xrp.getAttributeIntValue(null, "firstgid", 1);
		ts.name = xrp.getAttributeValue(null, "name");
		ts.tilewidth = xrp.getAttributeIntValue(null, "tilewidth", -1);
		ts.tileheight = xrp.getAttributeIntValue(null, "tileheight", -1);
		readCurrentTagUntilEnd(xrp, new TagHandler() {
			public void handleTag(XmlResourceParser xrp, String tagName) {
				if (tagName.equals("image")) {
					ts.imageSource = xrp.getAttributeValue(null, "source");
					ts.imageName = ts.imageSource;
					
					int v = ts.imageName.lastIndexOf('/');
					if (v >= 0) ts.imageName = ts.imageName.substring(v+1);
				}
			}
		});
		return ts;
	}
	
	private static TMXObjectGroup readTMXObjectGroup(XmlResourceParser xrp) throws XmlPullParserException, IOException {
		final TMXObjectGroup group = new TMXObjectGroup();
		group.name = xrp.getAttributeValue(null, "name");
		group.width = xrp.getAttributeIntValue(null, "width", 1);
		group.height = xrp.getAttributeIntValue(null, "height", 1);
		readCurrentTagUntilEnd(xrp, new TagHandler() {
			public void handleTag(XmlResourceParser xrp, String tagName) throws XmlPullParserException, IOException {
				if (tagName.equals("object")) {
					group.objects.add(readTMXObject(xrp));
				}
			}
		});
		return group;
	}
	
	private static TMXObject readTMXObject(XmlResourceParser xrp) throws XmlPullParserException, IOException {
		final TMXObject object = new TMXObject();
		object.name = xrp.getAttributeValue(null, "name");
		object.type = xrp.getAttributeValue(null, "type");
		object.x = xrp.getAttributeIntValue(null, "x", -1);
		object.y = xrp.getAttributeIntValue(null, "y", -1);
		object.width = xrp.getAttributeIntValue(null, "width", -1);
		object.height = xrp.getAttributeIntValue(null, "height", -1);
		readCurrentTagUntilEnd(xrp, new TagHandler() {
			public void handleTag(XmlResourceParser xrp, String tagName) {
				if (tagName.equals("property")) {
					final TMXProperty property = new TMXProperty();
					object.properties.add(property);
					property.name = xrp.getAttributeValue(null, "name");
					property.value = xrp.getAttributeValue(null, "value");
				}
			}
		});
		return object;
	}
	
	private static TMXLayer readTMXMapLayer(XmlResourceParser xrp) throws XmlPullParserException, IOException {
		final TMXLayer layer = new TMXLayer();
		layer.name = xrp.getAttributeValue(null, "name");
		layer.width = xrp.getAttributeIntValue(null, "width", 1);
		layer.height = xrp.getAttributeIntValue(null, "height", 1);
		layer.gids = new int[layer.width][layer.height];
		readCurrentTagUntilEnd(xrp, new TagHandler() {
			public void handleTag(XmlResourceParser xrp, String tagName) throws XmlPullParserException, IOException {
				if (tagName.equals("data")) {
					readTMXMapLayerData(xrp, layer);
				}
			}
		});
		return layer;
	}
	
	private static void readTMXMapLayerData(XmlResourceParser xrp, final TMXLayer layer) throws XmlPullParserException, IOException {
		String compressionMethod = xrp.getAttributeValue(null, "compression");
		xrp.next();
		String data = xrp.getText().trim();
		final int len = layer.width * layer.height * 4;
		
		ByteArrayInputStream bi = new ByteArrayInputStream(Base64.decode(data));
		if (compressionMethod == null) compressionMethod = "none";
		
		InflaterInputStream zi;
		if (compressionMethod.equalsIgnoreCase("zlib")) {
			zi = new InflaterInputStream(bi);
		} else if (compressionMethod.equalsIgnoreCase("gzip")) {
			zi = new GZIPInputStream(bi, len);
		} else {
			throw new IOException("Unhandled compression method \"" + compressionMethod + "\" for map layer " + layer.name);
		}
		
		byte[] buffer = new byte[len];
		copyStreamToBuffer(zi, buffer, len);
		
		zi.close();
		bi.close();
		int i = 0;
		for(int y = 0; y < layer.height; ++y) {
			for(int x = 0; x < layer.width; ++x, i += 4) {
				int gid = readIntLittleEndian(buffer, i);
				//if (gid != 0) L.log(getHexString(buffer, i) + " -> " + gid);
				layer.gids[x][y] = gid;
				//L.log("(" + x + "," + y + ") : " + layer.gids[x][y]);
			}
		}
	}
	
	private static void copyStreamToBuffer(InflaterInputStream zi, byte[] buffer, int len) throws IOException {
		int offset = 0;
		int bytesToRead = len;
		while (bytesToRead > 0) {
			int b = zi.read(buffer, offset, bytesToRead);
			if (b <= 0) throw new IOException("Failed to read stream!");
			bytesToRead -= b;
			offset += b;
		}
	}
	
	private interface TagHandler {
		void handleTag(XmlResourceParser xrp, String tagName) throws XmlPullParserException, IOException;
	}
	private static void readCurrentTagUntilEnd(XmlResourceParser xrp, TagHandler handler) throws XmlPullParserException, IOException {
		String outerTagName = xrp.getName();
		String tagName;
		int eventType;
		while ((eventType = xrp.next()) != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_TAG) {
				tagName = xrp.getName();
				handler.handleTag(xrp, tagName);
			} else if (eventType == XmlResourceParser.END_TAG) {
				tagName = xrp.getName();
				if (tagName.equals(outerTagName)) return;
			}
		}
	}
	/*
	private static String getHexString(byte v) {
		String result = Integer.toHexString(v & 0xff);
		if (result.length() < 2) result = '0' + result;
		return result;
	}
	private static String getHexString(byte[] buffer, int offset) {
		return getHexString(buffer[offset]) 
			+ getHexString(buffer[offset+1]) 
			+ getHexString(buffer[offset+2]) 
			+ getHexString(buffer[offset+3]);
	}
	*/
	private static int readIntLittleEndian(byte[] buffer, int offset) {
		return  (buffer[offset + 0] << 0 ) & 0x000000ff |
				(buffer[offset + 1] << 8 ) & 0x0000ff00 |
				(buffer[offset + 2] << 16) & 0x00ff0000 |
				(buffer[offset + 3] << 24) & 0xff000000;
	}
	
	public static final class TMXMap extends TMXLayerMap {
		public int xmlResourceId;
		public String name;
		public String orientation;
		public int tilewidth;
		public int tileheight;
		public ArrayList<TMXObjectGroup> objectGroups = new ArrayList<TMXObjectGroup>();
	}
	public static class TMXLayerMap {
		public int width;
		public int height;
		public TMXTileSet[] tileSets;
		public TMXLayer[] layers;
	}
	public static final class TMXTileSet {
		public int firstgid;
		public String name;
		public int tilewidth;
		public int tileheight;
		public String imageSource;
		public String imageName;
	}
	public static final class TMXLayer {
		public String name;
		public int width;
		public int height;
		public int[][] gids;
	}
	public static final class TMXObjectGroup {
		public String name;
		public int width;
		public int height;
		public ArrayList<TMXObject> objects = new ArrayList<TMXObject>();
	}
	public static final class TMXObject {
		public String name;
		public String type;
		public int x;
		public int y;
		public int width;
		public int height;
		public ArrayList<TMXProperty> properties = new ArrayList<TMXProperty>();
	}
	public static final class TMXProperty {
		public String name;
		public String value;
	}
	/*
	 
	 <map version="1.0" orientation="orthogonal" width="10" height="10" tilewidth="32" tileheight="32">
		 <tileset firstgid="1" name="tiles" tilewidth="32" tileheight="32">
	  		<image source="tilesets/tiles.png"/>
		 </tileset>
		 <layer name="Tile Layer 1" width="10" height="10">
			<data encoding="base64" compression="gzip">
			   H4sIAAAAAAAAA/NgYGDwIBK7AbEnHkyOOmwYXR02MwZSHQyTah4xGADnAt2SkAEAAA==
			</data>
		 </layer>
		 <layer name="Tile Layer 2" width="10" height="10">
			<data encoding="base64" compression="gzip">
			   H4sIAAAAAAAAA2NgoA1gYUHlP2HGro6NBbt4MysqXw2oLhEqlgSlU4H0YjR12EAbUE0KFnXPgG5iRLJ/GQ6zHuNwOy7gxE6aemQAAJRT7VKQAQAA
			</data>
		 </layer>
	 </map>

	 */
}
