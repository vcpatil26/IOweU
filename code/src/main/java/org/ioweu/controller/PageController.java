package org.ioweu.controller;

import org.apache.log4j.Logger;
import org.ioweu.utils.IOweUConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
public class PageController {
	
	private final Logger logger = Logger.getLogger(getClass());
	
	@RequestMapping(value = "/home.jsp", method = RequestMethod.GET)
	public String home() {
		logger.debug("Loading home page");
		return IOweUConstants.HOME_PAGE;
	}
	
	@RequestMapping(value = "/loginFailure.jsp", method = RequestMethod.GET)
	public String loginFailure() {
		logger.info("Login Failed !");
		return IOweUConstants.LOGIN_FAILURE_PAGE;
	}

	@RequestMapping(value = "/dashboard.jsp", method = RequestMethod.GET)
	public String getContactUs() {
		logger.debug("Load dashboard page");
		return IOweUConstants.DASHBOARD;
	}

    @RequestMapping(value = "/transactionLog.jsp", method = RequestMethod.GET)
    public String getTransactionLogPage() {
        logger.debug("Load dashboard page");
        return IOweUConstants.TRANSACTIONLOG;
    }

    @RequestMapping(value = "/recentActivity.jsp", method = RequestMethod.GET)
    public String getRecentActivityPage() {
        logger.debug("Load dashboard page");
        return IOweUConstants.RECENTACTIVITY;
    }

    @RequestMapping(value = "/approveTransactions.jsp", method = RequestMethod.GET)
    public String getApproveTransactionPage() {
        logger.debug("Load dashboard page");
        return IOweUConstants.APPROVETRANSACTION;
    }

    @RequestMapping(value = "/categoryAnalysis.jsp", method = RequestMethod.GET)
    public String getCatAnalysisPage() {
        logger.debug("Load dashboard page");
        return IOweUConstants.CATEGORYANALYSIS;
    }

}
