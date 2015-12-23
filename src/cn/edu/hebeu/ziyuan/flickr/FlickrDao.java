package cn.edu.hebeu.ziyuan.flickr;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.tags.Tag;

import cn.edu.hebeu.ziyuan.flickr.utils.JdbcUtils;

public class FlickrDao {

	public String getNoDetailItemID() {
		JdbcUtils jdbcUtils = new JdbcUtils();
		Connection connection = jdbcUtils.getConnection();
		String id = "";
		String sql = "select photoId from Info where lon is NULL LIMIT 1";
		try {
			ResultSet resultSet = connection.prepareStatement(sql).executeQuery();
			resultSet.next();
			id = resultSet.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	public void saveDetail(Photo photo) {
		JdbcUtils jdbcUtils = new JdbcUtils();
		String sql = "UPDATE Info SET lon = ?,lat=?,title =? ,description=?,dateTaken = ?,tags = ?,url=? where photoID =?";
		// 设置参数
		List<Object> params = new ArrayList<Object>();
		params.add(photo.getGeoData().getLongitude());
		params.add(photo.getGeoData().getLatitude());
		params.add(photo.getTitle());
		params.add(photo.getDescription());
		params.add(photo.getDateTaken());
		// 处理Tags
		Collection<Tag> tags = photo.getTags();
		StringBuilder sb = new StringBuilder();
		for (Tag tag : tags) {
			sb.append(tag.getId());
			sb.append(",");
		}
		if (sb.length() != 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		params.add(sb.toString());

		params.add(photo.getUrl());
		params.add(photo.getId());
		try {
			jdbcUtils.updateByPreparedStatement(sql, params);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}
