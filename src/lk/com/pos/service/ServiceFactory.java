package lk.com.pos.service;

public class ServiceFactory {
    private static ServiceFactory instance;
    
    private ServiceFactory() {}
    
    public static ServiceFactory getInstance() {
        if (instance == null) {
            synchronized (ServiceFactory.class) {
                if (instance == null) {
                    instance = new ServiceFactory();
                }
            }
        }
        return instance;
    }
    
    public FinancialDashboardService getFinancialDashboardService() {
        return new FinancialDashboardServiceImpl();
    }
}