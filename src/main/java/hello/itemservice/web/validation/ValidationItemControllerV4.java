package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import hello.itemservice.web.validation.form.ItemSaveForm;
import hello.itemservice.web.validation.form.ItemUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
public class ValidationItemControllerV4 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v4/addForm";
    }

    // @Validated 를 사용하면 @ModelAttribute 에서 바로 검증을 할 수 있음
    // -> @ModelAttribute 에서 검증을 하고, 바로 BindingResult 로 결과를 받을 수 있음
    // @Valid 사용가능 -> build.gradle 에서 spring-boot-starter-validation 의존성 추가 필요
//    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        // item 의 바인딩 된 결과가 bindingResult 에 담겨있음
        // bindingResult 가 ValidationItemControllerV1 에서 사용한 errors 와 같은 역할을 함
        // bindingResult 는 ModelAttribute 바로 뒤에 와야함 item 객체의 바인딩 결과를 담고 있기 때문에
        // bindingResult 가 없으면 400 에러가 발생하면서 컨트롤러 호출되지 않고, 오류 페이지로 이동함

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
            return "validation/v4/addForm"; // 다시 입력 폼으로
        }

        // 성공로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }

    // @Validated 를 사용하면 @ModelAttribute 에서 바로 검증을 할 수 있음
    // -> @ModelAttribute 에서 검증을 하고, 바로 BindingResult 로 결과를 받을 수 있음
    // @Valid 사용가능 -> build.gradle 에서 spring-boot-starter-validation 의존성 추가 필요
//    @PostMapping("/add")
    public String addItem2(@Validated(SaveCheck.class) @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // item 의 바인딩 된 결과가 bindingResult 에 담겨있음
        // bindingResult 가 ValidationItemControllerV1 에서 사용한 errors 와 같은 역할을 함
        // bindingResult 는 ModelAttribute 바로 뒤에 와야함 item 객체의 바인딩 결과를 담고 있기 때문에
        // bindingResult 가 없으면 400 에러가 발생하면서 컨트롤러 호출되지 않고, 오류 페이지로 이동함

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
            return "validation/v4/addForm"; // 다시 입력 폼으로
        }

        // 성공로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }

    // @Validated 를 사용하면 @ModelAttribute 에서 바로 검증을 할 수 있음
    // -> @ModelAttribute 에서 검증을 하고, 바로 BindingResult 로 결과를 받을 수 있음
    // @Valid 사용가능 -> build.gradle 에서 spring-boot-starter-validation 의존성 추가 필요
    @PostMapping("/add")
    public String addItem3(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // item 의 바인딩 된 결과가 bindingResult 에 담겨있음
        // bindingResult 가 ValidationItemControllerV1 에서 사용한 errors 와 같은 역할을 함
        // bindingResult 는 ModelAttribute 바로 뒤에 와야함 item 객체의 바인딩 결과를 담고 있기 때문에
        // bindingResult 가 없으면 400 에러가 발생하면서 컨트롤러 호출되지 않고, 오류 페이지로 이동함

        // 특정 필드가 아닌 복합 룰 검증
        if (form.getPrice() != null && form.getQuantity() != null) { // 가격 * 수량의 합이 10,000원 이상인지 확인
            int resultPrice = form.getPrice() * form.getQuantity();
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
            return "validation/v4/addForm"; // 다시 입력 폼으로
        }

        // 성공로직

        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setPrice(form.getPrice());
        item.setQuantity(form.getQuantity());

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }



    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/editForm";
    }

//    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute Item item, BindingResult bindingResult) {

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

        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v4/editForm";
        }

        itemRepository.update(itemId, item);

        return "redirect:/validation/v4/items/{itemId}";
    }

//    @PostMapping("/{itemId}/edit")
    public String editV2(@PathVariable Long itemId, @Validated(UpdateCheck.class) @ModelAttribute Item item, BindingResult bindingResult) {

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

        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v4/editForm";
        }

        itemRepository.update(itemId, item);

        return "redirect:/validation/v4/items/{itemId}";
    }

    @PostMapping("/{itemId}/edit")
    public String editV3(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {

        // 특정 필드가 아닌 복합 룰 검증
        if (form.getPrice() != null && form.getQuantity() != null) { // 가격 * 수량의 합이 10,000원 이상인지 확인
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < 10000) {
                // 특정 필드가 아닌 복합 룰 검증은 FieldError 가 아닌 ObjectError 를 사용해야 함
                // ObjectError 의 생성자에 들어가는 첫번째 파라미터는 objectName 이어야 함
//                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v4/editForm";
        }

        Item itemParam = new Item();
        itemParam.setItemName(form.getItemName());
        itemParam.setPrice(form.getPrice());
        itemParam.setQuantity(form.getQuantity());

        itemRepository.update(itemId, itemParam);

        return "redirect:/validation/v4/items/{itemId}";
    }

}

