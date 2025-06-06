package com.ljx.dto;

import com.ljx.entity.Setmeal;
import com.ljx.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
