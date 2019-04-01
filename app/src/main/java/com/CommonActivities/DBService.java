package com.CommonActivities;


import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBService {

    private Connection conn = null; //打开数据库对象
    private PreparedStatement ps = null;//操作整合sql语句的对象
    private ResultSet rs = null;//查询结果的集合
    private int result = -1;

    //DBService 对象
    public static DBService dbService = null;

    /**
     * 构造方法 私有化
     */

    private DBService() {

    }

    /**
     * 获取MySQL数据库单例类对象
     */

    public static DBService getDbService() {
        if (dbService == null) {
            dbService = new DBService();
        }
        return dbService;
    }

    /**
     * 获取要发送短信的患者信息    获取
     */

    public User getUserData(User dist) {
        //结果存放集合
        User usr = new User();
        //MySQL 语句
        String sql = "select * from user where Account = ?";
        //获取链接数据库对象
        conn = DBOpenHelper.getConn();
        try {
            if (conn != null && (!conn.isClosed())) {
                ps = (PreparedStatement) conn.prepareStatement(sql);
                ps.setString(1,dist.getAccount());
                if (ps != null) {
                    rs = ps.executeQuery();
                    while(rs.next()) {
                        usr.setID(rs.getInt("ID"));
                        usr.setAccount(rs.getString("Account"));
                        usr.setPassword(rs.getString("PassWord"));
                        usr.setBright(rs.getInt("Brightness"));
                        usr.setColor(rs.getInt("Color_Tempreture"));
                        usr.setIn(rs.getBinaryStream("Img"));
                        usr.setImg(ImageUtil.readBin2Image(usr.getIn(), usr.getAccount()));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }catch (NullPointerException e)
        {
            //do nothing
        }finally{
            try {
                if (usr.getIn() != null) {
                    usr.getIn().close();
                }
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        DBOpenHelper.closeAll(conn, ps, rs);//关闭相关操作
        return usr;
    }

    /**
     * 批量向数据库插入数据   增
     */

    public int insertUserData(User usr) {
        result = -1;
        PreparedStatement ps1 = null;
        Connection conn1 = null;
        int all = 0;
        if (usr != null) {
            //获取链接数据库对象
            conn = DBOpenHelper.getConn();
            conn1 = DBOpenHelper.getConn();

            try {
                String sql1 = "Select Count(*) total from user";

                if (conn1 != null && !(conn1.isClosed())) {
                    ps1 = conn1.prepareStatement(sql1);
                    if (ps1 != null) {
                        rs = ps1.executeQuery();
                        rs.next();
                        all = rs.getInt("total");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }

            DBOpenHelper.closeAll(conn1, ps1, rs);

            //MySQL 语句
            String sql = "INSERT INTO user(ID,Account,PassWord,Brightness,Color_Tempreture,Img) VALUES(?,?,?,?,?,?)";
            try {
                boolean closed = conn.isClosed();
                if ((conn != null) && (!closed)) {
                    ps = (PreparedStatement) conn.prepareStatement(sql);
                    String account = usr.getAccount();
                    String pass = usr.getPassword();
                    int color = usr.getColor_temp();
                    int bright = usr.getBright();
                    FileInputStream in = ImageUtil.readImage(usr.getImg());
                    ps.setInt(1, ++all);
                    ps.setString(2, account);
                    ps.setString(3, pass);
                    ps.setInt(4, color);
                    ps.setInt(5, bright);
                    ps.setBinaryStream(6,in);

                    result = ps.executeUpdate();                    //返回1 执行成功

                }
            } catch (SQLException e) {
               e.printStackTrace();
                return -1;
            }catch (IOException e)
            {
                e.printStackTrace();
                return -1;
            }
        }

        DBOpenHelper.closeAll(conn, ps);//关闭相关操作

        return result;
    }


    /**
     * 删除数据  删
     */

    public int delUserData(User usr) {
        int result = -1;
        if (usr != null) {
            //MySQL 语句
            String sql = "delete from user where ID=?";
            //获取链接数据库对象
            conn = DBOpenHelper.getConn();
            try {
                boolean closed = conn.isClosed();
                if ((conn != null) && (!closed)) {
                    ps = (PreparedStatement) conn.prepareStatement(sql);
                    ps.setInt(1, usr.getID());
                    result = ps.executeUpdate();//返回1 执行成功
                    if(result > 0)
                    {
                        String sql1 = "update user set ID=ID-1 where ID > ?";
                        ps = conn.prepareStatement(sql1);
                        ps.setInt(1,usr.getID());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }
        DBOpenHelper.closeAll(conn, ps);//关闭相关操作
        return result;
    }

    /*************
     * 更新密码
     */
    public int updatePass(int id, String newPass)
        throws SQLException
    {
        int flag = -1;
        String sql = "UPDATE user SET PassWord = ? WHERE ID = ?";
        conn = DBOpenHelper.getConn();
        if(conn != null && !conn.isClosed())
        {
            ps = (PreparedStatement) conn.prepareStatement(sql);
            ps.setString(1, newPass);
            ps.setInt(2, id);
            flag = ps.executeUpdate();
        }

        DBOpenHelper.closeAll(conn, ps);

        return flag;
    }

    /********************
     * 更新色温， 亮度
     * @param ct
     * @param bn
     * @param id
     * @return
     * @throws SQLException
     */
    public int Update_Set(int ct, int bn, int id)
            throws SQLException
    {
        int flag = -1;
        String sql = "UPDATE user SET Color_Tempreture = ?, Brightness = ? WHERE ID = ?";
        conn = DBOpenHelper.getConn();
        if(conn != null && !conn.isClosed())
        {
            ps = (PreparedStatement) conn.prepareStatement(sql);
            ps.setInt(1, ct);
            ps.setInt(2, bn);
            ps.setInt(3, id);

            flag = ps.executeUpdate();
        }

        DBOpenHelper.closeAll(conn, ps);
        return flag;
    }
}