package lambda.service;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

public class TxProxyFactoryBean implements FactoryBean<Object>{
	
	private Object target;
	private String pattern;
	private PlatformTransactionManager txManager;
	private Class<?> serviceInterface;

	public void setTarget(Object target) {
		this.target = target;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setTxManager(PlatformTransactionManager txManager) {
		this.txManager = txManager;
	}

	public void setServiceInterface(Class<?> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	@Override
	public Object getObject() throws Exception {
		
		TxHandler txHandler = new TxHandler();
		txHandler.setPattern(pattern);
		txHandler.setTarget(target);
		txHandler.setTxManager(txManager);
		
		return Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class[] { serviceInterface },
				txHandler);
	}

	@Override
	public Class<?> getObjectType() {
		return serviceInterface;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
}
