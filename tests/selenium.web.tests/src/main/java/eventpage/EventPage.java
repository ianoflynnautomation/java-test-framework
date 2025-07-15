package eventpage;

import solution.bjjeire.selenium.web.components.Button;
import solution.bjjeire.selenium.web.components.Heading;
import solution.bjjeire.selenium.web.components.Label;
import solution.bjjeire.selenium.web.components.Select;
import solution.bjjeire.selenium.web.pages.WebPage;

public class EventPage extends WebPage{

    public Heading heading() {
        return create().byDataTestId(Heading.class, "events-page-header-title");
    }

    public Label eventAmount() {
        return create().byDataTestId(Label.class, "events-page-header-total");
    }

    private Select cityFilter() { return create().byDataTestId(Select.class, "select-filter-select"); }

    private Button allTypesButton(){ return create().byDataTestId(Button.class, "button-group-filter-button-all"); }

    public EventPage SelectCounty(String county)
    {
        cityFilter().selectByText(county);

        return this;
    }

    public EventPage FilterAllTypes()
    {
        allTypesButton().toBeClickable();
        allTypesButton().click();

        return this;
    }


    public enum County {
        // Republic of Ireland
        CARLOW,
        CAVAN,
        CLARE,
        CORK,
        DONEGAL,
        DUBLIN,
        GALWAY,
        KERRY,
        KILDARE,
        KILKENNY,
        LAOIS,
        LEITRIM,
        LIMERICK,
        LONGFORD,
        LOUTH,
        MAYO,
        MEATH,
        MONAGHAN,
        OFFALY,
        ROSCOMMON,
        SLIGO,
        TIPPERARY,
        WATERFORD,
        WESTMEATH,
        WEXFORD,
        WICKLOW,

    }
}
