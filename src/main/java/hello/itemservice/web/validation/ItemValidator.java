package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

// Validator 인터페이스를 별도로 제공하는 이유는 체계적으로 검증 기능을 도입하기 위해서
@Component
public class ItemValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        // 여러 검증기를 등록한다면 여기서 구분
        return Item.class.isAssignableFrom(clazz);
        // 파라미터로 넘어오는 class 가 Item 에 지원이 되는지
        // == 쓰는것보다 isAssignableFrom 을 쓰는게 더 좋음 (자식 클래스도 통과함)
        // item == clazz
        // item == subItem // 자식 클래스도 통과함
    }

    @Override
    public void validate(Object target, Errors errors) { // 검증 로직

        Item item = (Item) target;

        // rejectValue 는 field, reject 는 object

        // 검증 로직
        if (!StringUtils.hasText(item.getItemName())) { // itemName이 비어있는지 확인
//            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
//            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, new String[]{"required.item.itemName"}, null, null));
            // bindingFailure : 데이터 자체가 넘어온게 실패했냐 물어보는것
            // codes 나 arguments 는 메시지 화에서 defaultMessage 대체 할 수 있음
            errors.rejectValue("itemName", "required");
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) { // 가격이 1,000 ~ 1,000,000 인지 확인
//            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
//            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
            errors.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) { // 수량이 10,000개 이상인지 확인
//            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999까지 허용합니다."));
//            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{9999}, null));
            errors.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) { // 가격 * 수량의 합이 10,000원 이상인지 확인
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                // 특정 필드가 아닌 복합 룰 검증은 FieldError 가 아닌 ObjectError 를 사용해야 함
                // ObjectError 의 생성자에 들어가는 첫번째 파라미터는 objectName 이어야 함
//                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
                errors.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }


    }
}
