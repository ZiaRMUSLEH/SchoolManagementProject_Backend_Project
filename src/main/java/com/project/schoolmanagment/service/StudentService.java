package com.project.schoolmanagment.service;

import com.project.schoolmanagment.entity.concretes.AdvisoryTeacher;
import com.project.schoolmanagment.entity.concretes.Student;
import com.project.schoolmanagment.entity.enums.RoleType;
import com.project.schoolmanagment.exception.ResourceNotFoundException;
import com.project.schoolmanagment.payload.mappers.StudentDto;
import com.project.schoolmanagment.payload.request.StudentRequest;
import com.project.schoolmanagment.payload.response.ResponseMessage;
import com.project.schoolmanagment.payload.response.StudentResponse;
import com.project.schoolmanagment.repository.StudentRepository;
import com.project.schoolmanagment.utils.CheckParameterUpdateMethod;
import com.project.schoolmanagment.utils.Messages;
import com.project.schoolmanagment.utils.ServiceHelpers;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.LifecycleState;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

	private final AdvisoryTeacherService advisoryTeacherService;

	private final StudentRepository studentRepository;

	private final ServiceHelpers serviceHelpers;

	private final StudentDto studentDto;

	private final PasswordEncoder passwordEncoder;

	private final UserRoleService userRoleService;


	public ResponseMessage<StudentResponse>saveStudent(StudentRequest studentRequest){
		//we need to find the advisory teacher of this student
		AdvisoryTeacher advisoryTeacher = advisoryTeacherService.getAdvisoryTeacherById(studentRequest.getAdvisorTeacherId());
		//we need to check duplication
		//correct order since we have varargs
		serviceHelpers.checkDuplicate(studentRequest.getUsername()
									,studentRequest.getSsn()
									,studentRequest.getPhoneNumber()
									,studentRequest.getEmail());

		Student student = studentDto.mapStudentRequestToStudent(studentRequest);
		student.setAdvisoryTeacher(advisoryTeacher);
		student.setPassword(passwordEncoder.encode(studentRequest.getPassword()));
		student.setUserRole(userRoleService.getUserRole(RoleType.STUDENT));
		student.setActive(true);
		student.setStudentNumber(getLastNumber());

		return ResponseMessage.<StudentResponse>builder()
				.object(studentDto.mapStudentToStudentResponse(studentRepository.save(student)))
				.message("Student saved Successfully")
				.build();
	}

	private int getLastNumber(){
		//we are just checking the database if we have any student
		if(!studentRepository.findStudent()){
			//first student
			return 1000;
		}
		return studentRepository.getMaxStudentNumber() + 1;
	}

	public ResponseMessage changeStatus(Long studentId,boolean status){
		Student student = isStudentsExist(studentId);
		student.setActive(status);
		studentRepository.save(student);
		return ResponseMessage.builder()
				.message("Student is " + (status ? "active" : "passive"))
				.httpStatus(HttpStatus.OK)
				.build();
	}

	private Student isStudentsExist(Long studentId){
		return studentRepository
				.findById(studentId)
				.orElseThrow(()->new ResourceNotFoundException(String.format(Messages.NOT_FOUND_USER_MESSAGE,studentId)));
	}

	public List<StudentResponse> getAllStudents(){
		return studentRepository.findAll()
				.stream()
				.map(studentDto::mapStudentToStudentResponse)
				.collect(Collectors.toList());
	}

	public ResponseMessage<StudentResponse>updateStudent(Long studentId, StudentRequest studentRequest){
		Student student = isStudentsExist(studentId);

		AdvisoryTeacher advisoryTeacher = advisoryTeacherService.getAdvisoryTeacherById(studentRequest.getAdvisorTeacherId());

		if(!CheckParameterUpdateMethod.checkUniquePropertiesForStudent(student,studentRequest)){
			serviceHelpers.checkDuplicate(studentRequest.getUsername(),
					studentRequest.getSsn(),
					studentRequest.getPhoneNumber(),
					studentRequest.getEmail());
		}

		Student studentForUpdate = studentDto.mapStudentRequestToUpdatedStudent(studentRequest,studentId);
		studentForUpdate.setPassword(passwordEncoder.encode(studentRequest.getPassword()));
		studentForUpdate.setAdvisoryTeacher(advisoryTeacher);
		studentForUpdate.setStudentNumber(student.getStudentNumber());
		studentForUpdate.setUserRole(userRoleService.getUserRole(RoleType.STUDENT));
		studentForUpdate.setActive(true);
		studentRepository.save(studentForUpdate);

		return ResponseMessage.<StudentResponse>builder()
				.object(studentDto.mapStudentToStudentResponse(studentRepository.save(studentForUpdate)))
				.message("Student updated successfully")
				.httpStatus(HttpStatus.OK)
				.build();
	}



}
