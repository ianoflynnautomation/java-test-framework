package solutions.bjjeire.core.plugins.testng;

import org.testng.ITestListener;
import org.testng.ITestResult;
import solutions.bjjeire.core.plugins.TestResult;

public class TestResultListener implements ITestListener {
    @Override
    public void onTestSuccess(ITestResult result) {
        BaseTest.CURRENT_TEST_RESULT.set(TestResult.SUCCESS);
        BaseTest.CURRENT_TEST_TIME_RECORD.get().setStartTime(result.getStartMillis());
        BaseTest.CURRENT_TEST_TIME_RECORD.get().setEndTime(result.getEndMillis());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        BaseTest.CURRENT_TEST_RESULT.set(TestResult.FAILURE);
        BaseTest.CURRENT_TEST_TIME_RECORD.get().setStartTime(result.getStartMillis());
        BaseTest.CURRENT_TEST_TIME_RECORD.get().setEndTime(result.getEndMillis());
    }
}