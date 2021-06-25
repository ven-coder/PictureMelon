package luoyi.android.com.wsfzzs.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class SQL {
    private static SQL instance = null;
    private static final String DB_NAME = "sugar_sql.db";
    private Context mContext;
    private static DaoSession mDaoSession;

    private SQL(Context context) {
        mContext = context;
        init();
    }

    public static SQL init(Context context) {
        if (instance == null) {
            synchronized (SQL.class) {
                if (instance == null) {
                    instance = new SQL(context);
                }
            }
        }
        return instance;
    }

    public static DaoSession getSession() {
        if (instance != null) {
            return mDaoSession;
        }
        return null;
    }

    private void init() {
        SQLHelper helper = new SQLHelper(mContext, DB_NAME, null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
    }
}
