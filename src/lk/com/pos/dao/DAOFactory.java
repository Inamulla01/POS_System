package lk.com.pos.dao;

import lk.com.pos.dao.custom.impl.FinancialDashboardDAOImpl;

public class DAOFactory {
    private static DAOFactory instance;
    
    private DAOFactory() {}
    
    public static DAOFactory getInstance() {
        if (instance == null) {
            synchronized (DAOFactory.class) {
                if (instance == null) {
                    instance = new DAOFactory();
                }
            }
        }
        return instance;
    }
    
    public FinancialDashboardDAO getFinancialDashboardDAO() {
        return new FinancialDashboardDAOImpl();
    }
}