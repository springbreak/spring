package lambda.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxHandler implements InvocationHandler {
	
	private Object target;
	private String pattern;
	private PlatformTransactionManager txManager;

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setTxManager(PlatformTransactionManager txManager) {
		this.txManager = txManager;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		if (method.getName().startsWith(pattern)) {
			return invokeInTranscation(method, args);
		} else {
			return method.invoke(target, args);
		}
	}

	private Object invokeInTranscation(Method method, Object[] args) throws Throwable {
		
		TransactionStatus txStats = 
				txManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			Object ret = method.invoke(target, args);
			txManager.commit(txStats);
			return ret;
		} catch (InvocationTargetException e) {
			txManager.rollback(txStats);
			throw e.getTargetException();
		}
	}

}
