package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.Faq;

public class FaqDao {

    public static List<Faq> findAll() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM faq");
        ResultSet rs = ps.executeQuery();
        List<Faq> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new Faq(rs.getString("question"), rs.getString("answer")));
        }
        rs.close();
        ps.close();
        return list;
    }
}
