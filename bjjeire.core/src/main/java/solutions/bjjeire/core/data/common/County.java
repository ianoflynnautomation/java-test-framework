package solutions.bjjeire.core.data.common;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum County {
    None,
    Carlow,
    Cavan,
    Clare,
    Cork,
    Donegal,
    Dublin,
    Galway,
    Kerry,
    Kildare,
    Kilkenny,
    Laois,
    Leitrim,
    Limerick,
    Longford,
    Louth,
    Mayo,
    Meath,
    Monaghan,
    Offaly,
    Roscommon,
    Sligo,
    Tipperary,
    Waterford,
    Westmeath,
    Wexford,
    Wicklow,
    // Northern Ireland (6 counties)
    Antrim,
    Armagh,
    Derry,
    Down,
    Fermanagh,
    Tyrone
}
