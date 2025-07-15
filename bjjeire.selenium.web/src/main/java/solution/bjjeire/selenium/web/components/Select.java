package solution.bjjeire.selenium.web.components;

import solutions.bjjeire.core.utilities.InstanceFactory;

public class Select extends WebComponent{

    @Override
    public Class<?> getComponentClass() {
        return getClass();
    }

    public Option getSelected() {
        org.openqa.selenium.support.ui.Select nativeSelect = new org.openqa.selenium.support.ui.Select(findElement());
        var optionComponent = InstanceFactory.create(Option.class);
        optionComponent.setFindStrategy(getFindStrategy());
        optionComponent.setElementIndex(0);
        optionComponent.setWrappedElement(nativeSelect.getFirstSelectedOption());
        return optionComponent;
    }

    public void selectByText(String text) {
        defaultSelectByText(text);
    }

    public void selectByIndex(int index) {
        defaultSelectByIndex(index);
    }

}
