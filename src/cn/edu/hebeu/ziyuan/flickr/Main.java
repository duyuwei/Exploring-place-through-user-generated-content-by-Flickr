package cn.edu.hebeu.ziyuan.flickr;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Test;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.photos.geo.GeoInterface;

public class Main {

	String url = "jdbc:mysql://localhost:3306/Flickr";
	String username = "root";
	String password = "root";
	String apiKey;
	String sharedSecret;
	String proxyHost;
	int proxyPort;

	public Main() {
		Properties prop = new Properties();
		try {
			// 读取属性文件
			InputStream in = new BufferedInputStream(new FileInputStream("settings.properties"));
			prop.load(in); /// 加载属性列表
			this.apiKey = prop.getProperty("apiKey");
			this.sharedSecret = prop.getProperty("sharedSecret");
			this.proxyHost = prop.getProperty("proxyHost");
			this.proxyPort = Integer.parseInt(prop.getProperty("proxyPort"));
			in.close();
		} catch (Exception e) {
			System.out.println("配置文件读取错误！！");
			System.out.println(e);
		}
	}

	public static void main(String[] args) {

		Main demo = new Main();
		demo.getGeneral();
	}

	@Test
	public void getGeneral() {

		REST rest = new REST();
		rest.setProxy(proxyHost, proxyPort);
		Flickr f = new Flickr(apiKey, sharedSecret, rest);
		PhotosInterface photosInterface = f.getPhotosInterface();

		SearchParameters params = new SearchParameters();
		params.setBBox("114.0333", "39.7667", "116.5333", "40.0167");
		int pages = 0;
		int per_page = 250;// 每页记录数
		long total = 0;

		// 数据库连接
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.out.println("找不到驱动程序类 ，加载驱动失败！");
			e1.printStackTrace();
		}
		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql = "INSERT INTO Info(photoId,ownerId) VALUES(?,?) ";
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e1) {
			System.out.println("数据库连接失败！");
			e1.printStackTrace();
		}

		try {

			// 获得总页数
			PhotoList<Photo> temp = photosInterface.search(params, per_page, 1);
			pages = temp.getPages();
			per_page = temp.getPerPage();
			// 显示信息
			System.out.println("按每" + per_page + "页条记录检索，共" + pages + "页");
			System.out.println("共计" + temp.getTotal() + "条数据");

		} catch (FlickrException e) {
			System.out.println("首次图片信息获取出现问题");
			e.printStackTrace();
		}

		for (int i = 1; i <= pages; i++) {
			try {
				PhotoList<Photo> photoList = photosInterface.search(params, per_page, i);
				for (int j = 0; j < photoList.size(); j++) {
					Photo photo = photoList.get(j);
					System.out.println("第" + i + "页，第" + (j + 1) + "条");

					String photoId = photo.getId();
					System.out.print("照片ID：" + photoId);// 照片ID

					String ownerId = photo.getOwner().getId();// 用户ID
					System.out.print("用户ID：" + ownerId);

					System.out.println("正在尝试保存第" + ++total + "张图片信息");

					try {
						// photoId,ownerId
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, photoId);
						pstmt.setString(2, ownerId);

						pstmt.executeUpdate();
					} catch (SQLException e) {
						System.out.println("错误：第" + total + "张图片信息保存失败！！！");
						e.printStackTrace();
					}
					System.out.println("第" + total + "张图片信息保存成功");
					System.out.println("------------------------------------------------");
				}
			} catch (FlickrException e1) {
				e1.printStackTrace();
				System.out.println("第" + i + "页保存失败，正在重试！");
				i--;
			}
		}

	}

	@Test
	public void doSearch() {
		REST rest = new REST();
		rest.setProxy(proxyHost, proxyPort);
		Flickr f = new Flickr(apiKey, sharedSecret, rest);
		PhotosInterface photosInterface = f.getPhotosInterface();
		GeoInterface geoInterface = f.getGeoInterface();
		SearchParameters params = new SearchParameters();
		params.setLongitude("116.46");
		params.setLatitude("39.92");
		int pages = 0;
		int per_page = 3;// 每页记录数
		long total = 0;

		// 数据库连接
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.out.println("找不到驱动程序类 ，加载驱动失败！");
			e1.printStackTrace();
		}
		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql = "INSERT INTO Info(photoId,ownerId,lon,lat,title,description,dateTaken) VALUES(?,?,?,?,?,?,?) ";
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e1) {
			System.out.println("数据库连接失败！");
			e1.printStackTrace();
		}

		try {

			pages = photosInterface.search(params, per_page, 1).getPages();
			System.out.println("按每" + per_page + "页条记录检索，共" + pages + "页");
			System.out.println("共计" + photosInterface.search(params, per_page, 1).getTotal() + "条数据");
			for (int i = 1; i <= pages; i++) {

				PhotoList<Photo> photoList = photosInterface.search(params, per_page, i);

				System.out.println(photoList);
				for (int j = 0; j < per_page; j++) {
					Photo photo = photoList.get(j);
					System.out.println("第" + i + "页，第" + (j + 1) + "条");

					String photoId = photo.getId();
					System.out.print("照片ID：" + photoId);// 照片ID

					photo = photosInterface.getInfo(photoId, null);

					String ownerId = photo.getOwner().getId();// 用户ID
					System.out.print("用户ID：" + ownerId);

					photo.setGeoData(geoInterface.getLocation(photoId)); // 地理位置
					System.out.print("经度：" + photo.getGeoData().getLongitude());
					System.out.print("纬度：" + photo.getGeoData().getLatitude());
					System.out.print("标题：" + photo.getTitle());//
					System.out.print("描述：" + photo.getDescription());//
					System.out.print("拍摄时间：" + photo.getDateTaken());
					System.out.println();
					System.out.println("正在尝试保存第" + ++total + "张图片信息");

					try {
						// photoId,ownerId,lon,lat,title,description,dateTaken
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, photoId);
						pstmt.setString(2, ownerId);
						pstmt.setFloat(3, photo.getGeoData().getLongitude());
						pstmt.setFloat(4, photo.getGeoData().getLatitude());
						pstmt.setString(5, photo.getTitle());
						pstmt.setString(6, photo.getDescription());
						pstmt.setTimestamp(7, new java.sql.Timestamp(photo.getDateTaken().getTime()));
						pstmt.executeUpdate();
					} catch (SQLException e) {
						System.out.println("错误：第" + total + "张图片信息保存失败！！！");
						e.printStackTrace();
					}
					System.out.println("第" + total + "张图片信息保存成功");
					System.out.println("------------------------------------------------");

				}

			}

		} catch (FlickrException e) {
			System.out.println("图片信息获取出现问题");
			e.printStackTrace();
		}

	}

}
