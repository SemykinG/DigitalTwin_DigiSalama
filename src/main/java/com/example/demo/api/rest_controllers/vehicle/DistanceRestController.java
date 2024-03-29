package com.example.demo.api.rest_controllers.vehicle;

import com.example.demo.database.models.EventHistoryLog;
import com.example.demo.database.models.utils.Mapping;
import com.example.demo.database.models.utils.RestResponse;
import com.example.demo.database.models.utils.ValidationResponse;
import com.example.demo.database.models.vehicle.Distance;
import com.example.demo.database.models.vehicle.Vehicle;
import com.example.demo.database.services.EventHistoryLogService;
import com.example.demo.database.services.vehicle.DistanceService;
import com.example.demo.utils.Constants;
import com.example.demo.utils.DateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.JSON_API + "/distances")
public class DistanceRestController {

	private final String ENTITY = "distance";

	@Autowired
	private final EventHistoryLogService eventHistoryLogService;

	@Autowired
	private final DistanceService distanceService;

	@Autowired
	private ObjectMapper objectMapper;


	@PostMapping(value = {"/batch"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Distance>>> postList(@RequestBody List<Distance> distances) {

		if (distances == null || distances.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<Distance>> responseList = new ArrayList<>();

		for (Distance distance : distances) {
			RestResponse<Distance> restResponse = new RestResponse<>();
			restResponse.setBody(distance);
			
			ValidationResponse response = distanceService.validate(distance, Mapping.POST);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				Distance distanceFromDatabase = distanceService.save(distance);

				if (distanceFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(distanceFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					eventHistoryLogService.addDistanceLog("create " + ENTITY, ENTITY + " created:\n" + distanceFromDatabase);
				}
			}

			responseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Distance>> post(@RequestBody Distance distance) {

		RestResponse<Distance> restResponse = new RestResponse<>();
		restResponse.setBody(distance);
		
		ValidationResponse response = distanceService.validate(distance, Mapping.POST);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		Distance distanceFromDatabase = distanceService.save(distance);

		if (distanceFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(distanceFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			eventHistoryLogService.addDistanceLog("create " + ENTITY, ENTITY + " created:\n" + distanceFromDatabase);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}

	@PostMapping(value = "/{id}", consumes = "application/json")
	public void postByID(@RequestBody Distance distance, @PathVariable Long id) {
		throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "POST method with ID parameter not allowed");
	}



	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Distance> getAll() {
		return distanceService.getAll();
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Distance getByID(@PathVariable Long id) {
		Distance distanceFromDatabase = distanceService.getById(id);

		if (distanceFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		return distanceFromDatabase;
	}



	@PutMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Distance>>> putList(@RequestBody List<Distance> distances) {

		if (distances == null || distances.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<Distance>> responseList = new ArrayList<>();

		for (Distance distance : distances) {
			RestResponse<Distance> restResponse = new RestResponse<>();
			restResponse.setBody(distance);
			
			ValidationResponse response = distanceService.validate(distance, Mapping.PUT);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				String oldDistanceFromDatabase = distanceService.getById(distance.getId()).toString();
				Distance distanceFromDatabase = distanceService.save(distance);

				if (distanceFromDatabase == null) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to save " + ENTITY + " in database");

					errorOccurred = true;
				} else {
					restResponse.setBody(distanceFromDatabase);
					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " saved successfully");

					eventHistoryLogService.addDistanceLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldDistanceFromDatabase + "\nto:\n" + distanceFromDatabase);
				}
			}

			responseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Distance>> putById(@RequestBody Distance distance, @PathVariable Long id) {

		RestResponse<Distance> restResponse = new RestResponse<>();
		restResponse.setBody(distance);
		
		ValidationResponse response = distanceService.validate(distance, Mapping.PUT);

		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		String oldDistanceFromDatabase = distanceService.getById(distance.getId()).toString();
		Distance distanceFromDatabase = distanceService.save(distance);

		if (distanceFromDatabase == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setBody(distanceFromDatabase);
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			eventHistoryLogService.addDistanceLog("update (PUT) " + ENTITY, ENTITY + " updated from:\n" + oldDistanceFromDatabase + "\nto:\n" + distanceFromDatabase);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@PatchMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<?>>> patchList(@RequestBody List<Map<String, Object>> changesList) {

		if (changesList == null || changesList.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		List<RestResponse<?>> responseList = new ArrayList<>();
		boolean errorOccurred = false;

		for (Map<String, Object> changes : changesList) {

			RestResponse<Map<String, Object>> mapResponse = new RestResponse<>();
			mapResponse.setBody(changes);

			if (changes == null) {
				mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				mapResponse.setMessage("NULL array element was provided");
				responseList.add(mapResponse);
				errorOccurred = true;
				continue;
			}

			if (!changes.containsKey("id")) {
				mapResponse.setHttp_status(HttpStatus.METHOD_NOT_ALLOWED);
				mapResponse.setMessage("ID parameter is required");

				responseList.add(mapResponse);

				errorOccurred = true;
			} else {
				Object idObj = changes.get("id");

				if (!(idObj instanceof Integer)) {
					mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
					mapResponse.setMessage("ID parameter is invalid");

					responseList.add(mapResponse);

					errorOccurred = true;
				} else {
					long idLong = (long) ((Integer) idObj);
					changes.remove("id");

					String oldDistanceFromDatabase = distanceService.getById(idLong).toString();
					Distance distanceFromDatabase;

					try {
						distanceFromDatabase = handlePatchChanges(idLong, changes);
					} catch (JsonParseException jsonParseException) {
						mapResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						mapResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());
						responseList.add(mapResponse);
						continue;
					}

					RestResponse<Distance> restResponse = new RestResponse<>();
					restResponse.setBody(distanceFromDatabase);
					
					ValidationResponse response = distanceService.validate(distanceFromDatabase, Mapping.PATCH);

					if (!response.isValid()) {
						restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
						restResponse.setMessage(response.getMessage());

						errorOccurred = true;
					} else {
						Distance updatedDistanceFromDatabase = distanceService.save(distanceFromDatabase);

						if (updatedDistanceFromDatabase == null) {
							restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
							restResponse.setMessage("failed to save " + ENTITY + " in database");

							errorOccurred = true;
						} else {
							restResponse.setBody(updatedDistanceFromDatabase);
							restResponse.setHttp_status(HttpStatus.OK);
							restResponse.setMessage(ENTITY + "patched successfully");

							eventHistoryLogService.addDistanceLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldDistanceFromDatabase + "\nto:\n" + updatedDistanceFromDatabase);
						}
					}

					responseList.add(restResponse);
				}
			}
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Distance>> patchById(@RequestBody Map<String, Object> changes, @PathVariable Long id) {

		if (changes == null || changes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		Distance distanceFromDatabase = distanceService.getById(id);

		if (distanceFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		String oldDistanceFromDatabase = distanceFromDatabase.toString();

		changes.remove("id");

		RestResponse<Distance> restResponse = new RestResponse<>();

		try {
			distanceFromDatabase = handlePatchChanges(id, changes);
		} catch (JsonParseException jsonParseException) {
			restResponse.setBody(distanceFromDatabase);
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(jsonParseException.getMessage() + " " + jsonParseException.getCause());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		restResponse.setBody(distanceFromDatabase);
		
		ValidationResponse response = distanceService.validate(distanceFromDatabase, Mapping.PATCH);
		
		if (!response.isValid()) {
			restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
			restResponse.setMessage(response.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
		}

		Distance patchedDistance = distanceService.save(distanceFromDatabase);
		restResponse.setBody(patchedDistance);

		if (patchedDistance == null) {
			restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
			restResponse.setMessage("failed to save " + ENTITY + " in database");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(restResponse);
		} else {
			restResponse.setHttp_status(HttpStatus.OK);
			restResponse.setMessage(ENTITY + " saved successfully");

			eventHistoryLogService.addDistanceLog("update (PATCH) " + ENTITY, ENTITY + " updated from:\n" + oldDistanceFromDatabase + "\nto:\n" + patchedDistance);

			return ResponseEntity.status(HttpStatus.OK).body(restResponse);
		}
	}



	@DeleteMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<RestResponse<Distance>>> deleteList(@RequestBody List<Distance> distances) {

		if (distances == null || distances.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NULL or empty array was provided");
		}

		boolean errorOccurred = false;

		List<RestResponse<Distance>> responseList = new ArrayList<>();

		for (Distance distance : distances) {
			RestResponse<Distance> restResponse = new RestResponse<>();
			restResponse.setBody(distance);
			
			ValidationResponse response = distanceService.validate(distance, Mapping.DELETE);

			if (!response.isValid()) {
				restResponse.setHttp_status(HttpStatus.BAD_REQUEST);
				restResponse.setMessage(response.getMessage());

				errorOccurred = true;
			} else {
				try {
					distanceService.delete(distance);

					restResponse.setHttp_status(HttpStatus.OK);
					restResponse.setMessage(ENTITY + " deleted successfully");

					eventHistoryLogService.addDistanceLog("delete " + ENTITY, ENTITY + " deleted:\n" + distance);
				} catch (Exception e) {
					restResponse.setHttp_status(HttpStatus.INTERNAL_SERVER_ERROR);
					restResponse.setMessage("failed to delete " + ENTITY + " from database \n" + e.getMessage());

					errorOccurred = true;
				}
			}

			responseList.add(restResponse);
		}

		if (errorOccurred) {
			return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(responseList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(responseList);
		}
	}

	@DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RestResponse<Distance>> deleteById(@PathVariable Long id) {
		Distance distanceFromDatabase = distanceService.getById(id);

		if (distanceFromDatabase == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ENTITY + " with ID: '" + id + "' not found");
		}

		ValidationResponse response = distanceService.validate(distanceFromDatabase, Mapping.DELETE);

		if (!response.isValid()) {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, response.getMessage());
		}

		try {
			distanceService.delete(distanceFromDatabase);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to delete " + ENTITY + " from database \n" + e.getMessage());
		}

		RestResponse<Distance> restResponse = new RestResponse<>();
		restResponse.setBody(distanceFromDatabase);
		restResponse.setHttp_status(HttpStatus.OK);
		restResponse.setMessage(ENTITY + " deleted successfully");

		eventHistoryLogService.addDistanceLog("delete " + ENTITY, ENTITY + " deleted:\n" + distanceFromDatabase);

		return ResponseEntity.ok(restResponse);
	}


	private Distance handlePatchChanges(Long id, Map<String, Object> changes) throws JsonParseException {
		Distance entity = new Distance(distanceService.getById(id));

		changes.forEach((key, value) -> {
			Field field = ReflectionUtils.findField(entity.getClass(), key);

			if (field != null) {
				field.setAccessible(true);

				String json = value == null ? null : value.toString();

				if (json == null) {
					ReflectionUtils.setField(field, entity, null);
				} else {
					if (field.getType().equals(String.class)) {
						ReflectionUtils.setField(field, entity, json);
					} else {

						if (field.getType().equals(LocalDateTime.class)) {
							LocalDateTime localDateTime = null;

							try {
								localDateTime = DateUtils.stringToLocalDateTime((String) value);
							} catch (Exception e) {
								throw new JsonParseException(new Throwable(e.getMessage()));
							}

							ReflectionUtils.setField(field, entity, localDateTime);
						}

						if (field.getType().equals(Vehicle.class)) {
							try {
								Vehicle vehicle = objectMapper.readValue(json, Vehicle.class);
								entity.setVehicle(vehicle);
							} catch (JsonProcessingException e) {
								throw new JsonParseException(new Throwable("Vehicle parsing error: " + e.getMessage()));
							}
						}

						if (field.getType().equals(Integer.class)) {
							try {
								Integer intValue = Integer.parseInt(json);
								ReflectionUtils.setField(field, entity, intValue);
							} catch (NumberFormatException e) {
								throw new JsonParseException(new Throwable("Integer value: '" + json + "' json parsing error: " + e.getMessage()));
							}
						}

					}
				}
			}
		});

		return entity;
	}
}
