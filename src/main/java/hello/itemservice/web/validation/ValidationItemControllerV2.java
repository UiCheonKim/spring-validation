package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    // 글로벌 설정해놓은 얜 필요 없어진다.
    @InitBinder // 이 컨트롤러 호출 될때마다 항상 불러짐
    public void init(WebDataBinder dataBinder) {
        // 컨트롤러 요청 될 때 WebDataBinder 가 내부적으로 만들어지고
        // 그 때 항상 itemValidator 검증기 를 넣어줌
        // 다른 메서드 호출 시 검증기 호출이 가능하다.
        dataBinder.addValidators(itemValidator);
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

//    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        // item 의 바인딩 된 결과가 bindingResult 에 담겨있음
        // bindingResult 가 ValidationItemControllerV1 에서 사용한 errors 와 같은 역할을 함
        // bindingResult 는 ModelAttribute 바로 뒤에 와야함 item 객체의 바인딩 결과를 담고 있기 때문에
        // bindingResult 가 없으면 400 에러가 발생하면서 컨트롤러 호출되지 않고, 오류 페이지로 이동함

        // 검증 로직
        if (!StringUtils.hasText(item.getItemName())) { // itemName이 비어있는지 확인
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) { // 가격이 1,000 ~ 1,000,000 인지 확인
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) { // 수량이 10,000개 이상인지 확인
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999까지 허용합니다."));
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) { // 가격 * 수량의 합이 10,000원 이상인지 확인
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                // 특정 필드가 아닌 복합 룰 검증은 FieldError 가 아닌 ObjectError 를 사용해야 함
                // ObjectError 의 생성자에 들어가는 첫번째 파라미터는 objectName 이어야 함
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            // model.addAttribute 에 안담아도 된다. // bindingResult 는 자동으로 view 에 넘어감
            return "validation/v2/addForm"; // 다시 입력 폼으로
        }

        // 성공로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        // item 의 바인딩 된 결과가 bindingResult 에 담겨있음
        // bindingResult 가 ValidationItemControllerV1 에서 사용한 errors 와 같은 역할을 함
        // bindingResult 는 ModelAttribute 바로 뒤에 와야함 item 객체의 바인딩 결과를 담고 있기 때문에
        // bindingResult 가 없으면 400 에러가 발생하면서 컨트롤러 호출되지 않고, 오류 페이지로 이동함

        // 검증 로직
        if (!StringUtils.hasText(item.getItemName())) { // itemName이 비어있는지 확인
//            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, null, null, "상품 이름은 필수입니다."));
            // bindingFailure : 데이터 자체가 넘어온게 실패했냐 물어보는것
            // codes 나 arguments 는 메시지 화에서 defaultMessage 대체 할 수 있음

        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) { // 가격이 1,000 ~ 1,000,000 인지 확인
//            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, null, null, "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) { // 수량이 10,000개 이상인지 확인
//            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999까지 허용합니다."));
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, null, null, "수량은 최대 9,999까지 허용합니다."));
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) { // 가격 * 수량의 합이 10,000원 이상인지 확인
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                // 특정 필드가 아닌 복합 룰 검증은 FieldError 가 아닌 ObjectError 를 사용해야 함
                // ObjectError 의 생성자에 들어가는 첫번째 파라미터는 objectName 이어야 함
                bindingResult.addError(new ObjectError("item", null, null, "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            // model.addAttribute 에 안담아도 된다. // bindingResult 는 자동으로 view 에 넘어감
            return "validation/v2/addForm"; // 다시 입력 폼으로
        }

        // 성공로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        // item 의 바인딩 된 결과가 bindingResult 에 담겨있음
        // bindingResult 가 ValidationItemControllerV1 에서 사용한 errors 와 같은 역할을 함
        // bindingResult 는 ModelAttribute 바로 뒤에 와야함 item 객체의 바인딩 결과를 담고 있기 때문에
        // bindingResult 가 없으면 400 에러가 발생하면서 컨트롤러 호출되지 않고, 오류 페이지로 이동함

        log.info("objectName={}", bindingResult.getObjectName()); // Item
        log.info("target={}", bindingResult.getTarget()); // Item 객체의 값

        // 검증 로직
        if (!StringUtils.hasText(item.getItemName())) { // itemName이 비어있는지 확인
//            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, new String[]{"required.item.itemName"}, null, null));
            // bindingFailure : 데이터 자체가 넘어온게 실패했냐 물어보는것
            // codes 나 arguments 는 메시지 화에서 defaultMessage 대체 할 수 있음

        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) { // 가격이 1,000 ~ 1,000,000 인지 확인
//            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) { // 수량이 10,000개 이상인지 확인
//            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999까지 허용합니다."));
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{9999}, null));
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) { // 가격 * 수량의 합이 10,000원 이상인지 확인
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                // 특정 필드가 아닌 복합 룰 검증은 FieldError 가 아닌 ObjectError 를 사용해야 함
                // ObjectError 의 생성자에 들어가는 첫번째 파라미터는 objectName 이어야 함
                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
            }
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            // model.addAttribute 에 안담아도 된다. // bindingResult 는 자동으로 view 에 넘어감
            return "validation/v2/addForm"; // 다시 입력 폼으로
        }

        // 성공로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        // item 의 바인딩 된 결과가 bindingResult 에 담겨있음
        // bindingResult 가 ValidationItemControllerV1 에서 사용한 errors 와 같은 역할을 함
        // bindingResult 는 ModelAttribute 바로 뒤에 와야함 item 객체의 바인딩 결과를 담고 있기 때문에
        // bindingResult 가 없으면 400 에러가 발생하면서 컨트롤러 호출되지 않고, 오류 페이지로 이동함

        log.info("objectName={}", bindingResult.getObjectName()); // Item
        log.info("target={}", bindingResult.getTarget()); // Item 객체의 값

        // rejectValue 는 field, reject 는 object

        ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");
        // 아래 로직을 이렇게 윗처럼 쓸 수 있음 -> 공백 같은 단순한 기능만 제공

/*
        // 검증 로직
        if (!StringUtils.hasText(item.getItemName())) { // itemName이 비어있는지 확인
//            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
//            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, new String[]{"required.item.itemName"}, null, null));
            // bindingFailure : 데이터 자체가 넘어온게 실패했냐 물어보는것
            // codes 나 arguments 는 메시지 화에서 defaultMessage 대체 할 수 있음
            bindingResult.rejectValue("itemName", "required");
        }
*/

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) { // 가격이 1,000 ~ 1,000,000 인지 확인
//            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
//            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) { // 수량이 10,000개 이상인지 확인
//            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999까지 허용합니다."));
//            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{9999}, null));
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) { // 가격 * 수량의 합이 10,000원 이상인지 확인
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                // 특정 필드가 아닌 복합 룰 검증은 FieldError 가 아닌 ObjectError 를 사용해야 함
                // ObjectError 의 생성자에 들어가는 첫번째 파라미터는 objectName 이어야 함
//                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            // model.addAttribute 에 안담아도 된다. // bindingResult 는 자동으로 view 에 넘어감
            return "validation/v2/addForm"; // 다시 입력 폼으로
        }

        // 성공로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        // item 의 바인딩 된 결과가 bindingResult 에 담겨있음
        // bindingResult 가 ValidationItemControllerV1 에서 사용한 errors 와 같은 역할을 함
        // bindingResult 는 ModelAttribute 바로 뒤에 와야함 item 객체의 바인딩 결과를 담고 있기 때문에
        // bindingResult 가 없으면 400 에러가 발생하면서 컨트롤러 호출되지 않고, 오류 페이지로 이동함

        itemValidator.validate(item, bindingResult);

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            // model.addAttribute 에 안담아도 된다. // bindingResult 는 자동으로 view 에 넘어감
            return "validation/v2/addForm"; // 다시 입력 폼으로
        }

        // 성공로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    // @Validated 를 사용하면 @ModelAttribute 에서 바로 검증을 할 수 있음
    // -> @ModelAttribute 에서 검증을 하고, 바로 BindingResult 로 결과를 받을 수 있음
    // @Valid 사용가능 -> build.gradle 에서 spring-boot-starter-validation 의존성 추가 필요
    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        // item 의 바인딩 된 결과가 bindingResult 에 담겨있음
        // bindingResult 가 ValidationItemControllerV1 에서 사용한 errors 와 같은 역할을 함
        // bindingResult 는 ModelAttribute 바로 뒤에 와야함 item 객체의 바인딩 결과를 담고 있기 때문에
        // bindingResult 가 없으면 400 에러가 발생하면서 컨트롤러 호출되지 않고, 오류 페이지로 이동함

        // 검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            // model.addAttribute 에 안담아도 된다. // bindingResult 는 자동으로 view 에 넘어감
            return "validation/v2/addForm"; // 다시 입력 폼으로
        }

        // 성공로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

