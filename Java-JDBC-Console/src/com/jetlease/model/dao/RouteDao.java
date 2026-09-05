package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.Route;

public class RouteDao {

    private static Route mapResultSet(ResultSet rs) throws SQLException {
        Route r = new Route();
        r.setCode(rs.getString("code"));
        r.setCity(rs.getString("city"));
        r.setLat(rs.getDouble("lat"));
        r.setLng(rs.getDouble("lng"));
        return r;
    }

    public static List<Route> findAll() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM routes ORDER BY city");
        ResultSet rs = ps.executeQuery();
        List<Route> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        rs.close();
        ps.close();
        return list;
    }

    public static Route findByCode(String code) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM routes WHERE code = ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        Route r = null;
        if (rs.next()) {
            r = mapResultSet(rs);
        }
        rs.close();
        ps.close();
        return r;
    }

    public static void save(Route r) throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("INSERT INTO routes (code,city,lat,lng) VALUES (?,?,?,?)");
        ps.setString(1, r.getCode());
        ps.setString(2, r.getCity());
        ps.setDouble(3, r.getLat());
        ps.setDouble(4, r.getLng());
        ps.executeUpdate();
        ps.close();
    }
}
