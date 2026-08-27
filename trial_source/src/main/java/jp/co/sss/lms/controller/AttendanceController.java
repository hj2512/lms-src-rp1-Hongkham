package jp.co.sss.lms.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.co.sss.lms.dto.AttendanceManagementDto;
import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.form.AttendanceForm;
import jp.co.sss.lms.service.StudentAttendanceService;
import jp.co.sss.lms.util.AttendanceUtil;
import jp.co.sss.lms.util.Constants;

/**
 * 勤怠管理コントローラ
 * 
 * @author 東京ITスクール
 */
@Controller
@RequestMapping("/attendance")
public class AttendanceController {

	@Autowired
	private StudentAttendanceService studentAttendanceService;

	@Autowired
	private LoginUserDto loginUserDto;

	@Autowired
	private AttendanceUtil attendanceUtil;

	/**
	 * 勤怠管理画面 初期表示
	 * 過去日の勤怠に未入力があるかチェックする
	 */
	@RequestMapping(path = "/detail", method = RequestMethod.GET)
	public String index(Model model) throws ParseException {

		// 勤怠一覧を取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService.getAttendanceManagement(
				loginUserDto.getCourseId(),
				loginUserDto.getLmsUserId());

		model.addAttribute(
				"attendanceManagementDtoList",
				attendanceManagementDtoList);

		// Task25：過去日の未入力チェック
		boolean notEnterCheck = studentAttendanceService.notEnterCheck();

		model.addAttribute("notEnterCheck", notEnterCheck);

		return "attendance/detail";
	}

	/**
	 * 勤怠管理画面 『出勤』ボタン押下
	 */
	@RequestMapping(path = "/detail", params = "punchIn", method = RequestMethod.POST)
	public String punchIn(Model model) {

		String error = studentAttendanceService.punchCheck(
				Constants.CODE_VAL_ATWORK);

		model.addAttribute("error", error);

		if (error == null) {

			String message = studentAttendanceService.setPunchIn();

			model.addAttribute("message", message);
		}

		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService.getAttendanceManagement(
				loginUserDto.getCourseId(),
				loginUserDto.getLmsUserId());

		model.addAttribute(
				"attendanceManagementDtoList",
				attendanceManagementDtoList);

		return "attendance/detail";
	}

	/**
	 * 勤怠管理画面 『退勤』ボタン押下
	 */
	@RequestMapping(path = "/detail", params = "punchOut", method = RequestMethod.POST)
	public String punchOut(Model model) {

		String error = studentAttendanceService.punchCheck(
				Constants.CODE_VAL_LEAVING);

		model.addAttribute("error", error);

		if (error == null) {

			String message = studentAttendanceService.setPunchOut();

			model.addAttribute("message", message);
		}

		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService.getAttendanceManagement(
				loginUserDto.getCourseId(),
				loginUserDto.getLmsUserId());

		model.addAttribute(
				"attendanceManagementDtoList",
				attendanceManagementDtoList);

		return "attendance/detail";
	}

	/**
	 * 勤怠管理画面
	 * 『勤怠情報を直接編集する』リンク押下
	 */
	@RequestMapping(path = "/update")
	public String update(Model model) {

		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService.getAttendanceManagement(
				loginUserDto.getCourseId(),
				loginUserDto.getLmsUserId());

		AttendanceForm attendanceForm = studentAttendanceService.setAttendanceForm(
				attendanceManagementDtoList);

		model.addAttribute("attendanceForm", attendanceForm);

		// 時リスト
		List<String> hours = new ArrayList<>();

		for (int i = 0; i < 24; i++) {
			hours.add(String.format("%02d", i));
		}

		// 分リスト
		List<String> minutes = new ArrayList<>();

		for (int i = 0; i < 60; i++) {
			minutes.add(String.format("%02d", i));
		}

		model.addAttribute("hours", hours);
		model.addAttribute("minutes", minutes);

		return "attendance/update";
	}

	/**
	 * 勤怠情報直接変更画面 『更新』ボタン押下
	 */
	@RequestMapping(path = "/update", params = "complete", method = RequestMethod.POST)
	public String complete(
			AttendanceForm attendanceForm,
			BindingResult result,
			Model model,
			RedirectAttributes redirectAttributes) throws ParseException {

		// Task27：勤怠入力チェック
		studentAttendanceService.updateInputCheck(
				attendanceForm,
				result);

		// エラーがある場合
		if (result.hasErrors()) {

			// 時リスト
			List<String> hours = new ArrayList<>();

			for (int i = 0; i < 24; i++) {
				hours.add(String.format("%02d", i));
			}

			// 分リスト
			List<String> minutes = new ArrayList<>();

			for (int i = 0; i < 60; i++) {
				minutes.add(String.format("%02d", i));
			}

			model.addAttribute("hours", hours);
			model.addAttribute("minutes", minutes);

			// 中抜け時間リストを再設定
			attendanceForm.setBlankTimes(
					attendanceUtil.setBlankTime());

			// BindingResultをModelに入れる
			model.addAttribute("result", result);

			return "attendance/update";
		}

		// エラーがなければ登録・更新
		String message = studentAttendanceService.update(attendanceForm);

		// リダイレクト先にメッセージを引き継ぐ
		redirectAttributes.addFlashAttribute("message", message);

		return "redirect:/attendance/detail";
	}
}